package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.WeeklyQuizCatalog
import com.example.ui.components.AchievementMedalEmblem
import com.example.ui.components.HeartRateWaveCanvas
import com.example.ui.components.StravaRouteCanvas
import com.example.ui.components.WeatherPlanningCard
import com.example.ui.components.pressScale
import com.example.ui.theme.CalorieAmber
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.HeartRose
import com.example.ui.theme.LocalPulseRunColors
import com.example.ui.theme.PenjasTealVibrant
import com.example.ui.theme.StepBlue
import com.example.ui.theme.StravaOrange
import com.example.ui.theme.StravaOrangeDark
import com.example.ui.theme.SunGold
import com.example.ui.viewmodel.HeartWaveState
import com.example.ui.viewmodel.LiveRunState
import com.example.ui.viewmodel.PulseRunUiState
import com.example.ui.viewmodel.QuizMode
import com.example.ui.viewmodel.WorkoutStatus
import kotlinx.coroutines.flow.StateFlow
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: PulseRunUiState,
    heartWaveFlow: StateFlow<HeartWaveState>,
    onToggleWorkout: () -> Unit,
    onFinishWorkout: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onNavigateToPenjas: () -> Unit,
    onOpenWeeklyQuiz: () -> Unit,
    onOpenAchievements: () -> Unit = {},
    onRefreshWeather: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    val run = uiState.liveRun
    val todayMetric = remember(uiState.dailyMetrics, uiState.todayDateKey) {
        uiState.dailyMetrics.find { it.dateKey == uiState.todayDateKey }
            ?: uiState.dailyMetrics.lastOrNull()
    }

    val totalStepsToday = (todayMetric?.steps ?: 6420) + run.sessionSteps
    val totalCaloriesToday = (todayMetric?.calories ?: 432) + run.sessionCalories
    val totalDistanceToday = (todayMetric?.distanceKm ?: 4.82) + run.sessionDistanceKm
    val stepGoal = uiState.preferences.stepGoal
    val calorieGoal = uiState.preferences.calorieGoal

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Live Workout Hero Card
            item(key = "hero_card", contentType = "hero") {
                LiveWorkoutHeroCard(
                    run = run,
                    onToggleWorkout = onToggleWorkout,
                    onFinishWorkout = onFinishWorkout,
                    onRequestLocationPermission = onRequestLocationPermission
                )
            }

            // 1b. Local Weather & Run Planning Card (Open-Meteo API)
            item(key = "weather_planning_card", contentType = "weather") {
                WeatherPlanningCard(
                    weather = uiState.weather,
                    hasLocationPermission = run.hasGpsPermission,
                    onRefreshWeather = onRefreshWeather,
                    onRequestLocationPermission = onRequestLocationPermission
                )
            }

            // 2. Penjas Warm-up Shortcut Banner
            item(key = "penjas_banner", contentType = "banner") {
                val bannerInteraction = remember { MutableInteractionSource() }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(bannerInteraction)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(PenjasTealVibrant, Color(0xFF059669))
                            )
                        )
                        .clickable(
                            interactionSource = bannerInteraction,
                            indication = LocalIndication.current,
                            onClick = onNavigateToPenjas
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("penjas_shortcut_banner"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🤸", fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                text = "Pemanasan Pagi Penjas",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Rekomendasi: ${uiState.warmupPlan.planTitle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCCFBF1)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Buka Penjas",
                        tint = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            // 2b. Weekly Health & Sports Quiz Card
            item(key = "weekly_quiz_card", contentType = "banner") {
                val weeklyTheme = WeeklyQuizCatalog.themeForWeek(uiState.activeCycleWeekIndex)
                val bestWeekPoints = remember(uiState.quizResults, uiState.currentWeekKey) {
                    uiState.quizResults
                        .filter { it.quizMode == QuizMode.WEEKLY.storageKey && it.weekKey == uiState.currentWeekKey }
                        .maxOfOrNull { it.points }
                }
                val quizCardInteraction = remember { MutableInteractionSource() }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(quizCardInteraction)
                        .clip(RoundedCornerShape(18.dp))
                        .background(palette.cardBackground)
                        .border(1.dp, StravaOrange.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                        .clickable(
                            interactionSource = quizCardInteraction,
                            indication = LocalIndication.current,
                            onClick = onOpenWeeklyQuiz
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("dashboard_weekly_quiz_card"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SunGold.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = weeklyTheme.emoji, fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                text = "Kuis Mingguan • ${weeklyTheme.weekLabel}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StravaOrange
                            )
                            Text(
                                text = weeklyTheme.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = palette.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (bestWeekPoints != null) {
                                    "Skor terbaik minggu ini: $bestWeekPoints poin"
                                } else {
                                    "Edukasi kesehatan & olahraga • Kumpulkan poin!"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (bestWeekPoints != null) EmeraldAccent else palette.textSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Buka Kuis Mingguan",
                        tint = StravaOrange
                    )
                }
            }

            // 2c. Achievements & Badges Summary Banner
            item(key = "achievements_summary_card", contentType = "banner") {
                val summary = uiState.achievements
                val featuredBadges = remember(summary.items) {
                    val unlocked = summary.items.filter { it.isUnlocked }.take(3)
                    if (unlocked.size >= 3) unlocked
                    else (unlocked + summary.items.filter { !it.isUnlocked }).take(3)
                }
                val badgeCardInteraction = remember { MutableInteractionSource() }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(badgeCardInteraction)
                        .clip(RoundedCornerShape(18.dp))
                        .background(palette.cardBackground)
                        .border(1.dp, SunGold.copy(alpha = 0.40f), RoundedCornerShape(18.dp))
                        .clickable(
                            interactionSource = badgeCardInteraction,
                            indication = LocalIndication.current,
                            onClick = onOpenAchievements
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("dashboard_achievements_card"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(SunGold, StravaOrange))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = "Pencapaian & Lencana",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "${summary.athleteRankTitle} • ${summary.unlockedCount}/${summary.totalCount} Lencana",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SunGold
                            )
                            Text(
                                text = "Pencapaian & Medali",
                                style = MaterialTheme.typography.titleSmall,
                                color = palette.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Streak Lari ${summary.runningStreakDays} hr • ${summary.totalBadgePoints} XP",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        featuredBadges.forEach { badge ->
                            AchievementMedalEmblem(
                                emoji = badge.definition.emoji,
                                tier = badge.definition.tier,
                                unlocked = badge.isUnlocked,
                                size = 30.dp
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Lihat Semua Lencana",
                            tint = SunGold
                        )
                    }
                }
            }

            // 3. Heart Rate Monitor Card (collects high-frequency waveform independently)
            item(key = "heart_rate_card", contentType = "heart") {
                HeartRateMonitorCard(
                    heartWaveFlow = heartWaveFlow,
                    minBpm = run.minBpm,
                    avgBpm = run.avgBpm,
                    maxBpm = run.maxBpm,
                    isActiveRun = run.status == WorkoutStatus.ACTIVE
                )
            }

            // 4. Steps & Calories Dual Metric Cards
            item(key = "metrics_row", contentType = "metrics") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    StepsSummaryCard(
                        steps = totalStepsToday,
                        stepGoal = stepGoal,
                        distanceKm = totalDistanceToday,
                        hasHardwareSensor = run.hasHardwareSteps,
                        modifier = Modifier.weight(1f)
                    )
                    CaloriesSummaryCard(
                        calories = totalCaloriesToday,
                        calorieGoal = calorieGoal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun LiveWorkoutHeroCard(
    run: LiveRunState,
    onToggleWorkout: () -> Unit,
    onFinishWorkout: () -> Unit,
    onRequestLocationPermission: () -> Unit
) {
    val palette = LocalPulseRunColors.current
    val status = run.status
    val gradientColors = if (palette.isDark) {
        listOf(Color(0xFF151C2C), Color(0xFF1A2336), Color(0xFF151C2C))
    } else {
        listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
    }

    val statusColor = when (status) {
        WorkoutStatus.IDLE -> EmeraldAccent
        WorkoutStatus.ACTIVE -> HeartRose
        WorkoutStatus.PAUSED -> CalorieAmber
    }
    val statusLabel = when (status) {
        WorkoutStatus.IDLE -> "SIAP LARI"
        WorkoutStatus.ACTIVE -> "MELACAK GPS"
        WorkoutStatus.PAUSED -> "DIJEDA"
    }
    val gpsLabel = when {
        run.hasGpsFix && run.gpsAccuracyMeters != null -> "GPS Akurat (±${run.gpsAccuracyMeters.toInt()}m)"
        run.hasGpsFix -> "GPS Terkunci"
        run.hasGpsPermission && status != WorkoutStatus.IDLE -> "Mencari Satelit GPS..."
        run.hasGpsPermission -> "GPS Siap"
        else -> "Aktifkan Izin GPS"
    }
    val gpsTint = when {
        run.hasGpsFix -> EmeraldAccent
        run.hasGpsPermission -> CalorieAmber
        else -> StravaOrange
    }

    val hrs = run.elapsedSeconds / 3600
    val mins = (run.elapsedSeconds % 3600) / 60
    val secs = run.elapsedSeconds % 60
    val formattedTimer = String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(gradientColors))
            .border(1.dp, Color(0xFF334155).copy(alpha = 0.55f), RoundedCornerShape(26.dp))
            .padding(20.dp)
    ) {
        // Lightweight radial-gradient atmospheric glow (zero blur pass jank)
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopEnd)
                .offset(x = 44.dp, y = (-44).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(StravaOrange.copy(alpha = 0.26f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.18f))
                        .border(1.dp, statusColor.copy(alpha = 0.35f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(enabled = !run.hasGpsPermission) { onRequestLocationPermission() }
                        .background(gpsTint.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("gps_status_pill"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GpsFixed,
                        contentDescription = gpsLabel,
                        tint = gpsTint,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = gpsLabel,
                        color = gpsTint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Primary Elapsed Time Readout (tnum avoids horizontal jitter during ticks)
            Column {
                Text(
                    text = formattedTimer,
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                    modifier = Modifier.testTag("workout_timer_text")
                )
                Text(
                    text = "Waktu Lari Real-time",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            // 3-Column Live GPS Telemetry Grid: Distance, Pace, Speed
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LiveMetricColumn(
                    label = "JARAK",
                    value = String.format(Locale.US, "%.2f", run.sessionDistanceKm),
                    unit = "km",
                    accent = StravaOrange,
                    tag = "session_distance_text",
                    modifier = Modifier.weight(1f)
                )
                LiveMetricColumn(
                    label = "PACE",
                    value = run.currentPaceDisplay,
                    unit = "/km (avg ${run.paceDisplay})",
                    accent = CalorieAmber,
                    tag = "session_pace_text",
                    modifier = Modifier.weight(1.2f)
                )
                LiveMetricColumn(
                    label = "KECEPATAN",
                    value = String.format(Locale.US, "%.1f", run.currentSpeedKmh),
                    unit = if (run.elevationGainMeters >= 1.0) "km/j • +${run.elevationGainMeters.toInt()}m" else "km/j",
                    accent = EmeraldAccent,
                    tag = "session_speed_text",
                    modifier = Modifier.weight(1f)
                )
            }

            // Live GPS Route Map Trace during active or paused run
            AnimatedVisibility(
                visible = status != WorkoutStatus.IDLE,
                enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(220)),
                exit = shrinkVertically() + fadeOut(tween(180))
            ) {
                StravaRouteCanvas(
                    points = run.routePoints,
                    routeLabel = if (run.hasGpsFix) {
                        String.format(
                            Locale.US,
                            "Rute GPS Langsung • %d titik • %.2f km",
                            run.gpsPointCount,
                            run.sessionDistanceKm
                        )
                    } else {
                        String.format(
                            Locale.US,
                            "Estimasi Lintasan • %.2f km",
                            run.sessionDistanceKm
                        )
                    }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val primaryBtnColor = if (status == WorkoutStatus.ACTIVE) CalorieAmber else StravaOrange
                val primaryBtnLabel = when (status) {
                    WorkoutStatus.IDLE -> "Mulai Lari Sekarang"
                    WorkoutStatus.ACTIVE -> "Jeda Lari"
                    WorkoutStatus.PAUSED -> "Lanjutkan Lari"
                }
                val primaryBtnInteraction = remember { MutableInteractionSource() }

                Button(
                    onClick = onToggleWorkout,
                    interactionSource = primaryBtnInteraction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBtnColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .pressScale(primaryBtnInteraction)
                        .testTag("toggle_workout_button")
                ) {
                    Icon(
                        imageVector = if (status == WorkoutStatus.ACTIVE) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = primaryBtnLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                AnimatedVisibility(
                    visible = status != WorkoutStatus.IDLE,
                    enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(200)),
                    exit = shrinkVertically() + fadeOut(tween(150))
                ) {
                    val finishBtnInteraction = remember { MutableInteractionSource() }
                    Button(
                        onClick = onFinishWorkout,
                        interactionSource = finishBtnInteraction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .pressScale(finishBtnInteraction)
                            .testTag("finish_workout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Selesai & Simpan ke Strava",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveMetricColumn(
    label: String,
    value: String,
    unit: String,
    accent: Color,
    tag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
        Text(
            text = value,
            color = accent,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            style = TextStyle(fontFeatureSettings = "tnum"),
            modifier = Modifier.testTag(tag)
        )
        Text(
            text = unit,
            color = Color(0xFFCBD5E1),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun HeartRateMonitorCard(
    heartWaveFlow: StateFlow<HeartWaveState>,
    minBpm: Int,
    avgBpm: Int,
    maxBpm: Int,
    isActiveRun: Boolean
) {
    val palette = LocalPulseRunColors.current
    val heartWave by heartWaveFlow.collectAsStateWithLifecycle()
    val currentBpm = heartWave.currentBpm
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActiveRun) 450 else 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_scale"
    )

    val zoneLabel = when {
        currentBpm < 110 -> "Zona 1 • Pemulihan Santai"
        currentBpm < 135 -> "Zona 2 • Pembakaran Lemak"
        currentBpm < 155 -> "Zona 3 • Kardio Aerobik"
        else -> "Zona 4 • Performa Puncak"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(palette.heartBg)
            .border(1.dp, palette.heartBorder, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(HeartRose.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = "Detak Jantung",
                        tint = HeartRose,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                scaleX = heartScale
                                scaleY = heartScale
                            }
                    )
                }
                Column {
                    Text(
                        text = "DETAK JANTUNG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = palette.textSecondary
                    )
                    Text(
                        text = zoneLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = HeartRose
                    )
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$currentBpm",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.textPrimary,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                    modifier = Modifier.testTag("bpm_value_text")
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "BPM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeartRose,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }
        }

        HeartRateWaveCanvas(samples = heartWave.waveform)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Min: $minBpm bpm",
                fontSize = 11.sp,
                color = palette.textSecondary
            )
            Text(
                text = "Rata-rata: $avgBpm bpm",
                fontSize = 11.sp,
                color = palette.textSecondary
            )
            Text(
                text = "Maks: $maxBpm bpm",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = HeartRose
            )
        }
    }
}

@Composable
private fun StepsSummaryCard(
    steps: Int,
    stepGoal: Int,
    distanceKm: Double,
    hasHardwareSensor: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    val pct = ((steps.toFloat() / stepGoal.toFloat()) * 100f).toInt().coerceIn(0, 100)
    val animatedProgress by animateFloatAsState(
        targetValue = (steps.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "steps_prog"
    )
    val formattedSteps = NumberFormat.getNumberInstance(Locale("id", "ID")).format(steps)
    val formattedGoal = NumberFormat.getNumberInstance(Locale("id", "ID")).format(stepGoal)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(palette.stepBg)
            .border(1.dp, palette.stepBorder, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StepBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DirectionsWalk,
                    contentDescription = "Langkah Lari",
                    tint = StepBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(StepBlue.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "$pct%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StepBlue
                )
            }
        }

        Column {
            Text(
                text = formattedSteps,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = palette.textPrimary,
                modifier = Modifier.testTag("steps_value_text")
            )
            Text(
                text = "dari $formattedGoal langkah",
                fontSize = 11.sp,
                color = palette.textSecondary
            )
            if (hasHardwareSensor) {
                Text(
                    text = "Pedometer Aktif",
                    fontSize = 10.sp,
                    color = StepBlue
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = StepBlue,
                trackColor = if (palette.isDark) Color(0xFF1E293B) else Color(0xFFDBEAFE),
                strokeCap = StrokeCap.Round
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Jarak Lari",
                    fontSize = 11.sp,
                    color = palette.textSecondary
                )
                Text(
                    text = String.format(Locale.US, "%.2f km", distanceKm),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
            }
        }
    }
}

@Composable
private fun CaloriesSummaryCard(
    calories: Int,
    calorieGoal: Int,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    val pct = ((calories.toFloat() / calorieGoal.toFloat()) * 100f).toInt().coerceIn(0, 100)
    val animatedProgress by animateFloatAsState(
        targetValue = (calories.toFloat() / calorieGoal.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "calories_prog"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(palette.calorieBg)
            .border(1.dp, palette.calorieBorder, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CalorieAmber.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = "Kalori Terbakar",
                    tint = CalorieAmber,
                    modifier = Modifier.size(20.dp)
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CalorieAmber.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "$pct%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CalorieAmber
                )
            }
        }

        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$calories",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.textPrimary,
                    modifier = Modifier.testTag("calories_value_text")
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "kkal",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CalorieAmber,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
            Text(
                text = "Target: $calorieGoal kkal",
                fontSize = 11.sp,
                color = palette.textSecondary
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(if (palette.isDark) Color(0xFF1E293B) else Color(0xFFFEF3C7))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(CalorieAmber, StravaOrangeDark)))
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Aktivitas",
                    fontSize = 11.sp,
                    color = palette.textSecondary
                )
                Text(
                    text = "+18% mgg ini",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldAccent
                )
            }
        }
    }
}
