package com.example.data.model

import com.example.data.local.WorkoutActivityEntity
import com.example.data.remote.LocalWeatherState
import java.util.Locale
import kotlin.math.roundToInt

enum class WarmupContextType {
    RAIN_INDOOR,
    COOL_WEATHER,
    HOT_WEATHER,
    IDEAL_WEATHER
}

data class RecommendedStretch(
    val exercise: PenjasExercise,
    val reasonTag: String,
    val whySuggested: String
)

data class AdaptiveWarmupPlan(
    val planTitle: String,
    val contextType: WarmupContextType,
    val weatherSummaryChip: String,
    val historySummaryChip: String,
    val coachNote: String,
    val recommendations: List<RecommendedStretch>,
    val totalDurationSeconds: Int
)

object WarmupRecommender {
    fun recommend(
        weather: LocalWeatherState,
        workouts: List<WorkoutActivityEntity>,
        nowMillis: Long = System.currentTimeMillis()
    ): AdaptiveWarmupPlan {
        val sevenDaysAgo = nowMillis - 7L * 86_400_000L
        val recentWorkouts = workouts.filter { it.timestampMillis >= sevenDaysAgo }
            .ifEmpty { workouts.take(3) }
        val latestRun = recentWorkouts.maxByOrNull { it.timestampMillis }
        val latestSpeedKmh = if (latestRun != null && latestRun.durationSeconds > 0L) {
            latestRun.distanceKm / (latestRun.durationSeconds / 3600.0)
        } else {
            0.0
        }
        val recentTotalKm = recentWorkouts.sumOf { it.distanceKm }

        val isLongRunLoad = (latestRun?.distanceKm ?: 0.0) >= 5.0 || recentTotalKm >= 12.0
        val isFastPaceLoad = latestSpeedKmh >= 10.5 || (latestRun?.avgBpm ?: 0) >= 145
        val hasRecentRuns = recentWorkouts.isNotEmpty()

        val hasWeather = weather.hasLoadedData
        val tempC = if (hasWeather) weather.temperatureC else 26.0
        val precipProb = if (hasWeather) weather.precipProbabilityPct else 10
        val precipMm = if (hasWeather) weather.precipitationMm else 0.0
        val isRainy = hasWeather && (
            precipMm >= 0.5 ||
                precipProb >= 50 ||
                weather.weatherCode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82, 95, 96, 99)
            )
        val isCool = hasWeather && tempC < 22.0
        val isHot = hasWeather && tempC >= 30.0

        val contextType = when {
            isRainy -> WarmupContextType.RAIN_INDOOR
            isHot -> WarmupContextType.HOT_WEATHER
            isCool -> WarmupContextType.COOL_WEATHER
            else -> WarmupContextType.IDEAL_WEATHER
        }

        val weatherChip = if (hasWeather) {
            "${weather.conditionEmoji} ${tempC.roundToInt()}°C • Hujan $precipProb%"
        } else {
            "🌤️ Cuaca Standar"
        }

        val historyChip = when {
            latestRun != null -> String.format(
                Locale.US,
                "🏃 Terakhir %.1f km • %d sesi",
                latestRun.distanceKm,
                recentWorkouts.size
            )
            else -> "🏃 Siap Memulai Lari"
        }

        val selected = linkedMapOf<Int, RecommendedStretch>()

        // 1. Weather-driven anchor exercises
        when (contextType) {
            WarmupContextType.RAIN_INDOOR -> {
                selected[1] = RecommendedStretch(
                    exercise = PenjasCatalog.findExercise(1), // Putar Lengan
                    reasonTag = "Anti-Selip Indoor",
                    whySuggested = "Aktifkan sendi bahu & sirkulasi tubuh di dalam ruangan saat peluang hujan $precipProb%."
                )
                selected[6] = RecommendedStretch(
                    exercise = PenjasCatalog.findExercise(6), // Squat
                    reasonTag = "Stabilisasi Lutut",
                    whySuggested = "Memanaskan otot paha dan lutut tanpa risiko terpeleset di lantai basah."
                )
            }
            WarmupContextType.COOL_WEATHER -> {
                selected[4] = RecommendedStretch(
                    exercise = PenjasCatalog.findExercise(4), // Lari di Tempat
                    reasonTag = "Naikkan Suhu Otot",
                    whySuggested = "Suhu sejuk (${tempC.roundToInt()}°C) butuh aktivasi kardio ringan agar otot tidak kaku."
                )
                selected[8] = RecommendedStretch(
                    exercise = PenjasCatalog.findExercise(8), // Ayunan Kaki
                    reasonTag = "Pelumas Pinggul",
                    whySuggested = "Melenturkan pinggul secara dinamis sebelum peregangan lebih dalam."
                )
            }
            WarmupContextType.HOT_WEATHER -> {
                selected[2] = RecommendedStretch(
                    exercise = PenjasCatalog.findExercise(2), // Peregangan Samping
                    reasonTag = "Napas & Terkontrol",
                    whySuggested = "Cuaca hangat (${tempC.roundToInt()}°C): regangkan torso lembut tanpa memicu kelelahan panas."
                )
                selected[10] = RecommendedStretch(
                    exercise = PenjasCatalog.findExercise(10), // Lunge Buka Pinggul
                    reasonTag = "Mobilitas Rendah Keringat",
                    whySuggested = "Membuka pinggul dengan intensitas terkontrol agar hemat energi sebelum lari."
                )
            }
            WarmupContextType.IDEAL_WEATHER -> {
                selected[8] = RecommendedStretch(
                    exercise = PenjasCatalog.findExercise(8), // Ayunan Kaki
                    reasonTag = "Mobilitas Dinamis",
                    whySuggested = "Cuaca nyaman (${tempC.roundToInt()}°C) ideal untuk pemanasan langkah lari dinamis."
                )
                selected[3] = RecommendedStretch(
                    exercise = PenjasCatalog.findExercise(3), // Putar Pinggang
                    reasonTag = "Aktivasi Inti",
                    whySuggested = "Menyiapkan otot perut dan punggung bawah untuk postur lari stabil."
                )
            }
        }

        // 2. Running-history-driven targeted stretches
        if (isLongRunLoad) {
            selected.putIfAbsent(
                10,
                RecommendedStretch(
                    exercise = PenjasCatalog.findExercise(10), // Lunge Buka Pinggul
                    reasonTag = "Pemulihan Jarak Jauh",
                    whySuggested = String.format(
                        Locale.US,
                        "Melepas ketegangan fleksor pinggul pasca lari %.1f km.",
                        latestRun?.distanceKm ?: recentTotalKm
                    )
                )
            )
            selected.putIfAbsent(
                11,
                RecommendedStretch(
                    exercise = PenjasCatalog.findExercise(11), // Peregangan Hamstring
                    reasonTag = "Rilekskan Hamstring",
                    whySuggested = "Mengembalikan kelenturan paha belakang setelah beban jarak mingguan."
                )
            )
        }

        if (isFastPaceLoad) {
            selected.putIfAbsent(
                9,
                RecommendedStretch(
                    exercise = PenjasCatalog.findExercise(9), // Peregangan Betis
                    reasonTag = "Lindungi Betis & Achilles",
                    whySuggested = String.format(
                        Locale.US,
                        "Pace terakhir cukup cepat (%.1f km/j) — kurangi ketegangan betis & tendon Achilles.",
                        latestSpeedKmh.coerceAtLeast(10.0)
                    )
                )
            )
            selected.putIfAbsent(
                11,
                RecommendedStretch(
                    exercise = PenjasCatalog.findExercise(11), // Peregangan Hamstring
                    reasonTag = "Elastisitas Langkah",
                    whySuggested = "Mendukung jangkauan langkah panjang tanpa menarik otot paha belakang."
                )
            )
        }

        if (!hasRecentRuns || selected.size < 4) {
            val fallbackPool = listOf(
                RecommendedStretch(
                    exercise = PenjasCatalog.findExercise(9),
                    reasonTag = "Kesiapan Betis",
                    whySuggested = "Menyiapkan otot betis dan pergelangan kaki agar tumpuan lari lebih aman."
                ),
                RecommendedStretch(
                    exercise = PenjasCatalog.findExercise(8),
                    reasonTag = "Ayunan Fleksor",
                    whySuggested = "Membuka gerak sendi pinggul untuk ritme langkah lebih ringan."
                ),
                RecommendedStretch(
                    exercise = PenjasCatalog.findExercise(11),
                    reasonTag = "Kelenturan Paha",
                    whySuggested = "Menjaga fleksibilitas hamstring dan punggung bawah."
                ),
                RecommendedStretch(
                    exercise = PenjasCatalog.findExercise(0),
                    reasonTag = "Rileks Leher & Bahu",
                    whySuggested = "Mengurangi ketegangan bahu agar ayunan lengan saat lari lebih efisien."
                )
            )
            for (candidate in fallbackPool) {
                if (selected.size >= 4) break
                selected.putIfAbsent(candidate.exercise.id, candidate)
            }
        }

        val finalList = selected.values.take(4)
        val totalDuration = finalList.sumOf { it.exercise.durationSeconds }

        val planTitle = when {
            isRainy -> "Paket Pemanasan Indoor & Mobilitas"
            isHot && isLongRunLoad -> "Paket Pemulihan Sejuk & Terkontrol"
            isHot -> "Paket Peregangan Hemat Energi"
            isCool -> "Paket Aktivasi Hangat Pra-Lari"
            isFastPaceLoad -> "Paket Betis & Hamstring Pace Tinggi"
            isLongRunLoad -> "Paket Pemulihan Pinggul & Kaki"
            else -> "Paket Pemanasan Dinamis Pelari"
        }

        val coachNote = buildString {
            when (contextType) {
                WarmupContextType.RAIN_INDOOR -> append("Peluang hujan $precipProb% terdeteksi, fokus ke gerakan stabil di dalam ruangan. ")
                WarmupContextType.COOL_WEATHER -> append("Suhu sejuk ${tempC.roundToInt()}°C membutuhkan pemanasan dinamis sebelum peregangan dalam. ")
                WarmupContextType.HOT_WEATHER -> append("Suhu cukup panas (${tempC.roundToInt()}°C), pilih peregangan terkontrol sambil menjaga hidrasi. ")
                WarmupContextType.IDEAL_WEATHER -> append("Cuaca mendukung (${tempC.roundToInt()}°C) untuk kombinasi mobilitas dinamis & peregangan kaki. ")
            }
            if (isLongRunLoad && latestRun != null) {
                append(
                    String.format(
                        Locale.US,
                        "Dipadukan fokus pemulihan pinggul & hamstring setelah lari %.1f km.",
                        latestRun.distanceKm
                    )
                )
            } else if (isFastPaceLoad) {
                append("Ditambah perlindungan betis & Achilles menyesuaikan intensitas lari cepat terakhirmu.")
            } else {
                append("Dirancang untuk menyiapkan sendi, betis, dan pinggul sebelum sesi lari berikutnya.")
            }
        }

        return AdaptiveWarmupPlan(
            planTitle = planTitle,
            contextType = contextType,
            weatherSummaryChip = weatherChip,
            historySummaryChip = historyChip,
            coachNote = coachNote,
            recommendations = finalList,
            totalDurationSeconds = totalDuration
        )
    }
}
