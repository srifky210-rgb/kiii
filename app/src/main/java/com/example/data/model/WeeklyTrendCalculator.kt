package com.example.data.model

import com.example.data.local.DailyMetricEntity
import com.example.data.local.WorkoutActivityEntity
import com.example.ui.viewmodel.LiveRunState
import com.example.ui.viewmodel.WorkoutStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WeeklyTrendPoint(
    val dateKey: String,
    val dayLabel: String,
    val distanceKm: Double,
    val speedKmh: Double,
    val paceDisplay: String,
    val workoutCount: Int,
    val isEstimatedSpeed: Boolean,
    val isToday: Boolean
)

data class WeeklyTrendSummary(
    val points: List<WeeklyTrendPoint>,
    val totalDistanceKm: Double,
    val avgSpeedKmh: Double,
    val peakSpeedKmh: Double,
    val avgDistanceKm: Double
)

object WeeklyTrendCalculator {
    fun buildWeeklyTrend(
        dailyMetrics: List<DailyMetricEntity>,
        workouts: List<WorkoutActivityEntity>,
        todayDateKey: String,
        liveRun: LiveRunState = LiveRunState(),
        referenceTimeMillis: Long = System.currentTimeMillis()
    ): WeeklyTrendSummary {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayFmt = SimpleDateFormat("EEE", Locale.forLanguageTag("id-ID"))

        val metricsByDate = dailyMetrics.associateBy { it.dateKey }
        val workoutsByDate = workouts.groupBy { sdf.format(Date(it.timestampMillis)) }

        val dateKeys = (6 downTo 0).map { offset ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = referenceTimeMillis
                add(Calendar.DAY_OF_YEAR, -offset)
            }
            val key = sdf.format(cal.time)
            val fallbackLabel = dayFmt.format(cal.time).replaceFirstChar { it.uppercase() }.take(3)
            key to fallbackLabel
        }

        val points = dateKeys.map { (dateKey, fallbackLabel) ->
            val metric = metricsByDate[dateKey]
            val dayWorkouts = workoutsByDate[dateKey].orEmpty()
            val isToday = dateKey == todayDateKey

            val workoutDistKm = dayWorkouts.sumOf { it.distanceKm }
            val workoutDurationSec = dayWorkouts.sumOf { it.durationSeconds }
            val liveExtraDist = if (isToday && liveRun.status != WorkoutStatus.IDLE) {
                liveRun.sessionDistanceKm
            } else {
                0.0
            }
            val liveExtraSec = if (isToday && liveRun.status != WorkoutStatus.IDLE) {
                liveRun.elapsedSeconds
            } else {
                0L
            }

            val baseDistanceKm = maxOf(metric?.distanceKm ?: 0.0, workoutDistKm)
            val totalDayDistanceKm = (baseDistanceKm + liveExtraDist).coerceAtLeast(0.0)

            val combinedTimedDist = workoutDistKm + liveExtraDist
            val combinedTimedSec = workoutDurationSec + liveExtraSec

            val (speedKmh, isEstimated) = when {
                isToday && liveRun.status == WorkoutStatus.ACTIVE && liveRun.currentSpeedKmh > 0.4 -> {
                    liveRun.currentSpeedKmh.coerceIn(0.0, 28.0) to false
                }
                combinedTimedSec >= 10L && combinedTimedDist >= 0.02 -> {
                    (combinedTimedDist / (combinedTimedSec / 3600.0)).coerceIn(2.0, 28.0) to false
                }
                totalDayDistanceKm >= 0.2 -> {
                    val stepIntensity = ((metric?.steps ?: (totalDayDistanceKm / 0.00075).toInt()) / 10000.0)
                        .coerceIn(0.2, 1.3)
                    (7.2 + stepIntensity * 2.6).coerceIn(6.5, 11.2) to true
                }
                else -> 0.0 to true
            }

            val paceStr = if (speedKmh >= 1.0) {
                val secPerKm = (3600.0 / speedKmh).toInt().coerceIn(150, 1200)
                "${secPerKm / 60}'${(secPerKm % 60).toString().padStart(2, '0')}\""
            } else {
                "0'00\""
            }

            WeeklyTrendPoint(
                dateKey = dateKey,
                dayLabel = metric?.dayLabel?.ifBlank { fallbackLabel } ?: fallbackLabel,
                distanceKm = totalDayDistanceKm,
                speedKmh = speedKmh,
                paceDisplay = paceStr,
                workoutCount = dayWorkouts.size + if (isToday && liveRun.status != WorkoutStatus.IDLE) 1 else 0,
                isEstimatedSpeed = isEstimated,
                isToday = isToday
            )
        }

        val totalDist = points.sumOf { it.distanceKm }
        val activeSpeedPoints = points.filter { it.speedKmh > 0.1 }
        val avgSpeed = if (activeSpeedPoints.isNotEmpty()) {
            activeSpeedPoints.map { it.speedKmh }.average()
        } else {
            0.0
        }
        val peakSpeed = points.maxOfOrNull { it.speedKmh } ?: 0.0
        val avgDist = if (points.isNotEmpty()) totalDist / points.size else 0.0

        return WeeklyTrendSummary(
            points = points,
            totalDistanceKm = totalDist,
            avgSpeedKmh = avgSpeed,
            peakSpeedKmh = peakSpeed,
            avgDistanceKm = avgDist
        )
    }
}
