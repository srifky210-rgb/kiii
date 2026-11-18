package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PenjasCatalog
import com.example.data.model.WeeklyTrendCalculator
import com.example.ui.components.WeeklyBarChart
import com.example.ui.components.WeeklyRunTrendChartCard
import com.example.ui.theme.CalorieAmber
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.HeartRose
import com.example.ui.theme.LocalPulseRunColors
import com.example.ui.theme.StepBlue
import com.example.ui.theme.StravaOrange
import com.example.ui.viewmodel.PulseRunUiState
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AnalyticsScreen(
    uiState: PulseRunUiState,
    onResetPenjasProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    var confirmReset by remember { mutableStateOf(false) }

    LaunchedEffect(confirmReset) {
        if (confirmReset) {
            delay(3000L)
            confirmReset = false
        }
    }

    val todayKey = uiState.todayDateKey
    val doneTodayCount = PenjasCatalog.exercises.count { ex ->
        uiState.penjasLogs.any { it.exerciseName == ex.name && it.dateKey == todayKey }
    }
    val totalSessions = uiState.penjasLogs.sumOf { it.completionCount }
    val bestQuizResult = uiState.quizResults.maxByOrNull { it.points }
    val totalQuizPoints = uiState.quizResults.sumOf { it.points }

    val weeklyTotalSteps = uiState.dailyMetrics.takeLast(7).sumOf { it.steps } + uiState.liveRun.sessionSteps
    val weeklyAvgK = (weeklyTotalSteps / 7f) / 1000f
    val formattedWeeklyTotal = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(weeklyTotalSteps)
    val weeklyTrend = remember(
        uiState.dailyMetrics,
        uiState.workouts,
        todayKey,
        uiState.liveRun.sessionDistanceKm,
        uiState.liveRun.elapsedSeconds,
        uiState.liveRun.currentSpeedKmh,
        uiState.liveRun.status
    ) {
        WeeklyTrendCalculator.buildWeeklyTrend(
            dailyMetrics = uiState.dailyMetrics,
            workouts = uiState.workouts,
            todayDateKey = todayKey,
            liveRun = uiState.liveRun
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item(key = "analytics_header", contentType = "header") {
                Column {
                    Text(
                        text = "Analisis & Progres Penjas",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "Statistik tren jarak, kecepatan, langkah & checklist gerakan",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                }
            }

            // 0. Weekly Running Distance & Speed Trend Chart
            item(key = "weekly_run_trend_chart", contentType = "chart") {
                WeeklyRunTrendChartCard(trend = weeklyTrend)
            }

            // 1. Progres Semangat Pagi Card
            item(key = "penjas_progress_card", contentType = "card") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(palette.cardBackground)
                        .border(1.dp, palette.cardBorder, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progres Semangat Pagi",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.penjasTint
                        )
                        Text(
                            text = if (confirmReset) "Klik lagi untuk konfirmasi" else "Reset Progres",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HeartRose,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (!confirmReset) {
                                        confirmReset = true
                                    } else {
                                        confirmReset = false
                                        onResetPenjasProgress()
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("reset_penjas_button")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PenjasStatTile(
                            value = "$doneTodayCount/${PenjasCatalog.exercises.size}",
                            label = "Gerakan Hari Ini",
                            accent = palette.penjasTint,
                            modifier = Modifier.weight(1f)
                        )
                        PenjasStatTile(
                            value = "${uiState.streakDays} Hari",
                            label = "Berturut-turut",
                            accent = CalorieAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PenjasStatTile(
                            value = "${totalSessions}×",
                            label = "Total Latihan",
                            accent = StepBlue,
                            modifier = Modifier.weight(1f)
                        )
                        PenjasStatTile(
                            value = if (bestQuizResult == null) "–" else "${bestQuizResult.points} pts",
                            label = if (bestQuizResult == null) "Skor Kuis Terbaik" else "Terbaik (${bestQuizResult.score}/${bestQuizResult.totalQuestions})",
                            accent = EmeraldAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        PenjasCatalog.exercises.forEachIndexed { idx, ex ->
                            val logsForEx = uiState.penjasLogs.filter { it.exerciseName == ex.name }
                            val doneToday = logsForEx.any { it.dateKey == todayKey }
                            val countAll = logsForEx.sumOf { it.completionCount }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${if (doneToday) "✅" else "⬜"} ${ex.name}",
                                    fontSize = 12.sp,
                                    color = palette.textPrimary
                                )
                                Text(
                                    text = "${countAll}× selesai",
                                    fontSize = 11.sp,
                                    color = palette.textSecondary
                                )
                            }
                            if (idx < PenjasCatalog.exercises.lastIndex) {
                                HorizontalDivider(color = palette.cardBorder.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // 1b. Weekly Health Quiz Score History Card
            item(key = "quiz_history_card", contentType = "card") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(palette.cardBackground)
                        .border(1.dp, palette.cardBorder, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Skor Kuis Edukasi Kebugaran",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                            Text(
                                text = "Total akumulasi: $totalQuizPoints poin",
                                fontSize = 12.sp,
                                color = StravaOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (uiState.quizResults.isEmpty()) {
                        Text(
                            text = "Belum ada riwayat kuis. Kerjakan Kuis Mingguan di tab Penjas untuk mengumpulkan poin kesehatan!",
                            fontSize = 12.sp,
                            color = palette.textSecondary,
                            lineHeight = 18.sp
                        )
                    } else {
                        uiState.quizResults.take(5).forEachIndexed { idx, result ->
                            val pct = if (result.totalQuestions > 0) (result.score * 100) / result.totalQuestions else 0
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = result.quizTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textPrimary
                                    )
                                    Text(
                                        text = "${result.athleteName} • ${result.dateKey} • Benar ${result.score}/${result.totalQuestions} ($pct%)",
                                        fontSize = 11.sp,
                                        color = palette.textSecondary
                                    )
                                }
                                Text(
                                    text = "${result.points} pts",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldAccent
                                )
                            }
                            if (idx < minOf(4, uiState.quizResults.lastIndex)) {
                                HorizontalDivider(color = palette.cardBorder.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // 2. Weekly Steps Chart Card
            item(key = "weekly_chart_card", contentType = "card") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(palette.cardBackground)
                        .border(1.dp, palette.cardBorder, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Langkah Lari Mingguan",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                            Text(
                                text = "Total: $formattedWeeklyTotal langkah",
                                fontSize = 12.sp,
                                color = StepBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(StepBlue.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "Rata-rata %.1fk/hr", weeklyAvgK),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StepBlue
                            )
                        }
                    }

                    WeeklyBarChart(
                        metrics = uiState.dailyMetrics,
                        todayDateKey = todayKey
                    )
                }
            }

            // 3. Heart Rate Zones Today
            item(key = "heart_zones_card", contentType = "card") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(palette.cardBackground)
                        .border(1.dp, palette.cardBorder, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Zona Detak Jantung Hari Ini",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )

                    val activeBonusMins = (uiState.liveRun.elapsedSeconds / 60).toInt()
                    HeartZoneRow(
                        label = "Zona 5 • Maksimal (171-190 BPM)",
                        minutes = 4,
                        fraction = 0.12f,
                        color = HeartRose
                    )
                    HeartZoneRow(
                        label = "Zona 4 • Anaerobik (152-170 BPM)",
                        minutes = 14 + (activeBonusMins / 2),
                        fraction = 0.35f,
                        color = StravaOrange
                    )
                    HeartZoneRow(
                        label = "Zona 3 • Aerobik (133-151 BPM)",
                        minutes = 22 + activeBonusMins,
                        fraction = 0.58f,
                        color = EmeraldAccent
                    )
                    HeartZoneRow(
                        label = "Zona 2 • Bakar Lemak (114-132 BPM)",
                        minutes = 38 + totalSessions,
                        fraction = 0.80f,
                        color = StepBlue
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun PenjasStatTile(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (palette.isDark) Color(0xB30B0F19) else palette.subtleSurface)
            .padding(12.dp)
    ) {
        Text(
            text = value,
            fontFamily = FredokaFontFamily,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accent
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = palette.textSecondary
        )
    }
}

@Composable
private fun HeartZoneRow(
    label: String,
    minutes: Int,
    fraction: Float,
    color: Color
) {
    val palette = LocalPulseRunColors.current
    val animatedFraction by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(450),
        label = "zone_frac"
    )
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
            Text(
                text = "$minutes mnt",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
        }
        LinearProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = palette.subtleSurface,
            strokeCap = StrokeCap.Round
        )
    }
}
