package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdaptiveWarmupPlan
import com.example.data.model.PenjasCatalog
import com.example.data.model.PenjasExercise
import com.example.data.model.WeeklyQuizCatalog
import com.example.ui.components.PenjasCharacterCanvas
import com.example.ui.components.pressScale
import com.example.ui.theme.CalorieAmber
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.HeartRose
import com.example.ui.theme.LocalPulseRunColors
import com.example.ui.theme.PenjasCyan
import com.example.ui.theme.PenjasTealVibrant
import com.example.ui.theme.StravaOrange
import com.example.ui.theme.SunGold
import com.example.ui.viewmodel.PenjasPlayerState
import com.example.ui.viewmodel.PulseRunUiState
import com.example.ui.viewmodel.QuizMode
import com.example.ui.viewmodel.QuizUiState
import kotlinx.coroutines.launch

@Composable
fun PenjasScreen(
    uiState: PulseRunUiState,
    onSelectExercise: (Int) -> Unit,
    onToggleVideoDemo: () -> Unit,
    onGoDemoStep: (Int) -> Unit,
    onToggleDemoPause: () -> Unit,
    onToggleSingleExercise: () -> Unit,
    onSpeakExercise: () -> Unit,
    onStartFullSession: () -> Unit,
    onHaltExercise: () -> Unit,
    onSelectQuizMode: (QuizMode) -> Unit,
    onSelectWeeklyTheme: (Int) -> Unit,
    onSpeakQuizQuestion: () -> Unit,
    onAnswerQuizOption: (Int) -> Unit,
    onNextQuizQuestion: () -> Unit,
    onRestartQuiz: () -> Unit,
    onStartRecommendedWarmup: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    var showQuiz by rememberSaveable { mutableStateOf(false) }
    var lastHandledTrigger by rememberSaveable { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = showQuiz) {
        onHaltExercise()
        showQuiz = false
    }

    LaunchedEffect(uiState.penjasQuizTabTrigger) {
        if (uiState.penjasQuizTabTrigger > lastHandledTrigger) {
            lastHandledTrigger = uiState.penjasQuizTabTrigger
            onHaltExercise()
            showQuiz = true
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header & Sub-tab Switcher (Gerakan vs Kuis)
            item(key = "penjas_header", contentType = "header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Semangat Pagi Penjas",
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = palette.penjasTint
                        )
                        Text(
                            text = "Pemanasan interaktif dengan animasi & suara",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.cardBackground)
                            .border(1.dp, palette.cardBorder, RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SubTabButton(
                            label = "Gerakan",
                            selected = !showQuiz,
                            onClick = {
                                onHaltExercise()
                                showQuiz = false
                            },
                            modifier = Modifier.testTag("penjas_subtab_gerakan")
                        )
                        SubTabButton(
                            label = "Kuis",
                            selected = showQuiz,
                            onClick = {
                                onHaltExercise()
                                showQuiz = true
                            },
                            modifier = Modifier.testTag("penjas_subtab_kuis")
                        )
                    }
                }
            }

            if (!showQuiz) {
                // Interactive Context-Aware Warm-Up Recommendation Card (Weather + Run History)
                item(key = "smart_warmup_card", contentType = "smart_warmup") {
                    SmartWarmupRecommendationCard(
                        plan = uiState.warmupPlan,
                        selectedExerciseId = uiState.penjas.selectedExerciseIndex,
                        onSelectStretch = { exerciseId ->
                            onSelectExercise(exerciseId)
                            coroutineScope.launch { listState.animateScrollToItem(2) }
                        },
                        onStartRecommendedPackage = {
                            onStartRecommendedWarmup()
                            coroutineScope.launch { listState.animateScrollToItem(2) }
                        }
                    )
                }

                // Main Exercise Stage Card
                item(key = "penjas_player", contentType = "player") {
                    PenjasPlayerCard(
                        penjas = uiState.penjas,
                        onToggleVideoDemo = onToggleVideoDemo,
                        onGoDemoStep = onGoDemoStep,
                        onToggleDemoPause = onToggleDemoPause,
                        onToggleSingleExercise = onToggleSingleExercise,
                        onSpeakExercise = onSpeakExercise,
                        onStartFullSession = onStartFullSession
                    )
                }

                // Warm-up Section Header
                item(key = "warmup_header", contentType = "section_header") {
                    Text(
                        text = "DAFTAR GERAKAN PEMANASAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = palette.textSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                items(
                    items = PenjasCatalog.exercises.filter { it.category == "Pemanasan" },
                    key = { "warmup_${it.id}" },
                    contentType = { "exercise_item" }
                ) { exercise ->
                    ExerciseListItem(
                        exercise = exercise,
                        isSelected = exercise.id == uiState.penjas.selectedExerciseIndex,
                        onClick = {
                            onSelectExercise(exercise.id)
                            coroutineScope.launch { listState.animateScrollToItem(2) }
                        }
                    )
                }

                // Morning Cardio Section Header
                item(key = "cardio_header", contentType = "section_header") {
                    Text(
                        text = "DAFTAR OLAHRAGA PAGI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = palette.textSecondary,
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp, end = 4.dp)
                    )
                }

                items(
                    items = PenjasCatalog.exercises.filter { it.category != "Pemanasan" },
                    key = { "cardio_${it.id}" },
                    contentType = { "exercise_item" }
                ) { exercise ->
                    ExerciseListItem(
                        exercise = exercise,
                        isSelected = exercise.id == uiState.penjas.selectedExerciseIndex,
                        onClick = {
                            onSelectExercise(exercise.id)
                            coroutineScope.launch { listState.animateScrollToItem(2) }
                        }
                    )
                }

                // Runner-Specific Stretches Section Header
                item(key = "runner_stretches_header", contentType = "section_header") {
                    Text(
                        text = "PEREGANGAN KHUSUS PELARI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = palette.textSecondary,
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp, end = 4.dp)
                    )
                }

                items(
                    items = PenjasCatalog.runningStretches,
                    key = { "stretch_${it.id}" },
                    contentType = { "exercise_item" }
                ) { exercise ->
                    ExerciseListItem(
                        exercise = exercise,
                        isSelected = exercise.id == uiState.penjas.selectedExerciseIndex,
                        onClick = {
                            onSelectExercise(exercise.id)
                            coroutineScope.launch { listState.animateScrollToItem(2) }
                        }
                    )
                }
            } else {
                item(key = "penjas_quiz", contentType = "quiz") {
                    PenjasQuizCard(
                        quiz = uiState.quiz,
                        activeCycleWeekIndex = uiState.activeCycleWeekIndex,
                        athleteName = uiState.preferences.athleteName,
                        onSelectQuizMode = onSelectQuizMode,
                        onSelectWeeklyTheme = onSelectWeeklyTheme,
                        onSpeakQuestion = onSpeakQuizQuestion,
                        onAnswerOption = onAnswerQuizOption,
                        onNextQuestion = onNextQuizQuestion,
                        onRestartQuiz = onRestartQuiz
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun SubTabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    val bgColor by animateColorAsState(
        targetValue = if (selected) PenjasTealVibrant else Color.Transparent,
        animationSpec = tween(200),
        label = "subtab_bg"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else palette.textSecondary
        )
    }
}

@Composable
private fun PenjasPlayerCard(
    penjas: PenjasPlayerState,
    onToggleVideoDemo: () -> Unit,
    onGoDemoStep: (Int) -> Unit,
    onToggleDemoPause: () -> Unit,
    onToggleSingleExercise: () -> Unit,
    onSpeakExercise: () -> Unit,
    onStartFullSession: () -> Unit
) {
    val palette = LocalPulseRunColors.current
    val ex = penjas.selectedExercise

    val demoProgress = if (penjas.isDemoActive && penjas.demoTotalSteps > 0) {
        (penjas.demoStepIndex.toFloat() / penjas.demoTotalSteps.toFloat()).coerceIn(0f, 1f)
    } else if (penjas.demoCaption.startsWith("Video selesai")) {
        1f
    } else {
        0f
    }

    val timerProgress = if (penjas.isExerciseRunning && penjas.totalTimeSeconds > 0) {
        ((penjas.totalTimeSeconds - penjas.timeLeftSeconds).toFloat() / penjas.totalTimeSeconds.toFloat())
            .coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedTimerProgress by animateFloatAsState(
        targetValue = timerProgress,
        animationSpec = tween(350),
        label = "penjas_timer_bar"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(palette.cardBackground)
            .border(1.dp, palette.cardBorder, RoundedCornerShape(26.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Character Canvas
        PenjasCharacterCanvas(
            spec = ex.animationSpec,
            isPlaying = penjas.isAnimationPlaying,
            exerciseName = ex.name
        )

        // Caption Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = penjas.demoCaption,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }

        // Demo progress bar & step controls
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LinearProgressIndicator(
                progress = { demoProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(CircleShape),
                color = PenjasTealVibrant,
                trackColor = palette.subtleSurface,
                strokeCap = StrokeCap.Round
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DemoStepMiniButton(
                    label = "⏮",
                    onClick = { onGoDemoStep(-1) },
                    modifier = Modifier.testTag("demo_prev_button")
                )
                Spacer(modifier = Modifier.width(8.dp))
                DemoStepMiniButton(
                    label = if (penjas.isDemoPaused) "▶ Lanjut" else "⏸ Jeda",
                    onClick = onToggleDemoPause,
                    modifier = Modifier.testTag("demo_pause_button")
                )
                Spacer(modifier = Modifier.width(8.dp))
                DemoStepMiniButton(
                    label = "⏭",
                    onClick = { onGoDemoStep(1) },
                    modifier = Modifier.testTag("demo_next_button")
                )
            }
        }

        HorizontalDivider(color = palette.cardBorder)

        // Title, Category, Status & Countdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SunGold)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = ex.category,
                            color = Color(0xFF451A03),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (penjas.activeQueueLabel.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(PenjasTealVibrant.copy(alpha = 0.16f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = penjas.activeQueueLabel,
                                color = palette.penjasTint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = ex.name,
                    fontFamily = FredokaFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = penjas.statusText,
                    fontSize = 11.sp,
                    color = palette.textSecondary
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${penjas.timeLeftSeconds}",
                        fontFamily = FredokaFontFamily,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.penjasTint,
                        style = TextStyle(fontFeatureSettings = "tnum"),
                        modifier = Modifier.testTag("penjas_timer_text")
                    )
                    Text(
                        text = " dtk",
                        fontSize = 12.sp,
                        color = palette.textSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }

        Text(
            text = ex.description,
            fontSize = 12.sp,
            color = palette.textSecondary,
            lineHeight = 18.sp
        )

        LinearProgressIndicator(
            progress = { animatedTimerProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(CircleShape),
            color = PenjasTealVibrant,
            trackColor = palette.subtleSurface,
            strokeCap = StrokeCap.Round
        )

        // 2x2 Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onToggleVideoDemo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PenjasTealVibrant,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_video_demo")
                ) {
                    Text(
                        text = if (penjas.isDemoActive) "⏹ Hentikan" else "🎬 Video Demo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onToggleSingleExercise,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.subtleSurface,
                        contentColor = palette.textPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_try_exercise")
                ) {
                    Text(
                        text = if (penjas.isExerciseRunning) "■ Berhenti" else "▶ Coba Gerakan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSpeakExercise,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.subtleSurface,
                        contentColor = palette.textPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_speak_exercise")
                ) {
                    Text(
                        text = "🔊 Dengarkan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onStartFullSession,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SunGold,
                        contentColor = Color(0xFF451A03)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_full_session")
                ) {
                    Text(
                        text = "🏃 Sesi Lengkap",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Steps Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (palette.isDark) Color(0xB30B0F19) else palette.subtleSurface)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Langkah-langkah:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
            ex.steps.forEachIndexed { idx, step ->
                val isHighlighted = idx == penjas.highlightedStepIndex
                Text(
                    text = "${idx + 1}. $step",
                    fontSize = 12.sp,
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                    color = if (isHighlighted) PenjasCyan else palette.textSecondary,
                    lineHeight = 17.sp
                )
            }
        }

        // Benefits & Safety Details Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(palette.penjasBg)
                .border(1.dp, palette.penjasBorder, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DetailBullet(label = "Manfaat", value = ex.benefits, textColor = palette.textPrimary)
            DetailBullet(label = "Otot dilatih", value = ex.musclesWorked, textColor = palette.textPrimary)
            DetailBullet(
                label = "Target",
                value = "${ex.targetReps} • Tips: ${ex.tips}",
                textColor = palette.textPrimary
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = HeartRose, fontWeight = FontWeight.Bold)) {
                        append("Hindari: ")
                    }
                    append(ex.avoidTip)
                },
                fontSize = 12.sp,
                color = palette.textPrimary,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun DetailBullet(label: String, value: String, textColor: Color) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("$label: ")
            }
            append(value)
        },
        fontSize = 12.sp,
        color = textColor,
        lineHeight = 17.sp
    )
}

@Composable
private fun DemoStepMiniButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(palette.subtleSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary
        )
    }
}

@Composable
private fun ExerciseListItem(
    exercise: PenjasExercise,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalPulseRunColors.current
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) palette.penjasBg else palette.cardBackground,
        animationSpec = tween(200),
        label = "ex_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PenjasTealVibrant else palette.cardBorder,
        animationSpec = tween(200),
        label = "ex_border"
    )
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction, pressedScale = 0.98f)
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("penjas_item_${exercise.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(text = exercise.emoji, fontSize = 24.sp)
            Column {
                Text(
                    text = exercise.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Text(
                    text = "${exercise.durationSeconds} detik • ${exercise.targetReps}",
                    fontSize = 11.sp,
                    color = palette.textSecondary
                )
            }
        }
        Icon(
            imageVector = Icons.Rounded.PlayCircle,
            contentDescription = "Pilih ${exercise.name}",
            tint = PenjasTealVibrant,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun PenjasQuizCard(
    quiz: QuizUiState,
    activeCycleWeekIndex: Int,
    athleteName: String,
    onSelectQuizMode: (QuizMode) -> Unit,
    onSelectWeeklyTheme: (Int) -> Unit,
    onSpeakQuestion: () -> Unit,
    onAnswerOption: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onRestartQuiz: () -> Unit
) {
    val palette = LocalPulseRunColors.current
    val questions = quiz.questions
    val total = questions.size.coerceAtLeast(1)
    val weeklyTheme = quiz.activeWeeklyTheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(palette.cardBackground)
            .border(1.dp, palette.cardBorder, RoundedCornerShape(26.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Switcher: Weekly Health Quiz vs Basic Penjas Quiz
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(palette.subtleSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            QuizModePill(
                label = "🏆 Kuis Mingguan",
                selected = quiz.mode == QuizMode.WEEKLY,
                onClick = { onSelectQuizMode(QuizMode.WEEKLY) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("quiz_mode_weekly")
            )
            QuizModePill(
                label = "🤸 Dasar Penjas",
                selected = quiz.mode == QuizMode.PENJAS_BASIC,
                onClick = { onSelectQuizMode(QuizMode.PENJAS_BASIC) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("quiz_mode_basic")
            )
        }

        // Weekly Theme Selector & Fitness Education Header (when in Weekly mode)
        if (quiz.mode == QuizMode.WEEKLY) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeeklyQuizCatalog.weeks.forEach { theme ->
                    val isSelected = theme.weekIndex == quiz.selectedWeekIndex
                    val isThisWeek = theme.weekIndex == activeCycleWeekIndex
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) StravaOrange.copy(alpha = 0.16f)
                                else palette.subtleSurface
                            )
                            .border(
                                1.dp,
                                if (isSelected) StravaOrange else palette.cardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectWeeklyTheme(theme.weekIndex) }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("quiz_week_${theme.weekIndex}"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = theme.emoji, fontSize = 16.sp)
                        Text(
                            text = "M${theme.weekIndex}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) StravaOrange else palette.textPrimary
                        )
                        if (isThisWeek) {
                            Text(
                                text = "Minggu Ini",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldAccent
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.penjasBg)
                    .border(1.dp, palette.penjasBorder, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${weeklyTheme.emoji} ${weeklyTheme.weekLabel}: ${weeklyTheme.title}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.penjasTint
                )
                Text(
                    text = weeklyTheme.subtitle,
                    fontSize = 11.sp,
                    color = palette.textSecondary
                )
            }
        }

        if (quiz.isFinished) {
            val pct = (quiz.score * 100) / total
            val rankBadge = when {
                pct >= 90 -> "Master Kebugaran 🏆"
                pct >= 75 -> "Atlet Cerdas 💪"
                pct >= 50 -> "Pelari Aktif 🏃"
                else -> "Pemula Semangat 🌱"
            }
            val feedbackMessage = when {
                pct >= 90 -> "Luar biasa, $athleteName! Pemahaman kesehatan dan olahragamu sangat unggul."
                pct >= 75 -> "Hebat, $athleteName! Terus pertahankan kebiasaan sehatmu."
                pct >= 50 -> "Bagus! Pelajari poin edukasi kebugaran di bawah lalu coba lagi."
                else -> "Ayo semangat, $athleteName! Baca tips kebugaran dan ulangi kuisnya."
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(StravaOrange.copy(alpha = 0.14f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = rankBadge,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StravaOrange
                    )
                }

                Text(
                    text = "${quiz.points} Poin",
                    fontFamily = FredokaFontFamily,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.penjasTint,
                    modifier = Modifier.testTag("quiz_final_score")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuizScoreStatTile(
                        label = "Benar",
                        value = "${quiz.score}/$total",
                        modifier = Modifier.weight(1f)
                    )
                    QuizScoreStatTile(
                        label = "Akurasi",
                        value = "$pct%",
                        modifier = Modifier.weight(1f)
                    )
                    QuizScoreStatTile(
                        label = "Streak Maks",
                        value = "${quiz.bestStreak}×",
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = feedbackMessage,
                    fontSize = 13.sp,
                    color = palette.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )

                if (quiz.mode == QuizMode.WEEKLY) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.subtleSurface)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "💡 Ringkasan Edukasi Kebugaran",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        weeklyTheme.educationSummary.forEach { point ->
                            Text(
                                text = "• $point",
                                fontSize = 12.sp,
                                color = palette.textSecondary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = onRestartQuiz,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PenjasTealVibrant,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("quiz_restart_button")
                ) {
                    Text(text = "Ulangi Kuis", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        } else {
            val question = questions[quiz.currentIndex.coerceIn(0, total - 1)]
            val progress = quiz.currentIndex.toFloat() / total.toFloat()

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = PenjasTealVibrant,
                trackColor = palette.subtleSurface,
                strokeCap = StrokeCap.Round
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Soal ${quiz.currentIndex + 1} dari $total",
                    fontSize = 11.sp,
                    color = palette.textSecondary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (quiz.currentStreak >= 2) {
                        Text(
                            text = "🔥 ${quiz.currentStreak}×",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalorieAmber
                        )
                    }
                    Text(
                        text = "Skor: ${quiz.points} pts",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.penjasTint,
                        modifier = Modifier.testTag("quiz_points_text")
                    )
                }
            }

            if (question.categoryTag.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(StravaOrange.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = question.categoryTag,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = StravaOrange
                    )
                }
            }

            Text(
                text = question.question,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
                lineHeight = 22.sp
            )

            Button(
                onClick = onSpeakQuestion,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.subtleSurface,
                    contentColor = palette.textPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier
                    .height(44.dp)
                    .testTag("quiz_speak_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Bacakan Soal", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            val letters = listOf("A", "B", "C", "D")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                question.options.forEachIndexed { idx, opt ->
                    val selected = quiz.selectedOptionIndex
                    val isAnswered = selected != null
                    val isCorrectOption = idx == question.correctIndex
                    val isSelectedWrong = selected == idx && !isCorrectOption

                    val targetBg = when {
                        isAnswered && isCorrectOption -> Color(0xFF059669)
                        isAnswered && isSelectedWrong -> HeartRose
                        else -> if (palette.isDark) Color(0xCC0B0F19) else palette.subtleSurface
                    }
                    val targetBorder = when {
                        isAnswered && isCorrectOption -> Color(0xFF059669)
                        isAnswered && isSelectedWrong -> HeartRose
                        else -> palette.cardBorder
                    }
                    val bgColor by animateColorAsState(targetBg, tween(180), label = "opt_bg_$idx")
                    val borderColor by animateColorAsState(targetBorder, tween(180), label = "opt_bd_$idx")
                    val textColor = if (isAnswered && (isCorrectOption || isSelectedWrong)) {
                        Color.White
                    } else {
                        palette.textPrimary
                    }
                    val optInteraction = remember(quiz.currentIndex, idx) { MutableInteractionSource() }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressScale(optInteraction, pressedScale = 0.985f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                            .clickable(
                                enabled = !isAnswered,
                                interactionSource = optInteraction,
                                indication = LocalIndication.current
                            ) { onAnswerOption(idx) }
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                            .testTag("quiz_option_$idx"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${letters[idx]}.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isAnswered && (isCorrectOption || isSelectedWrong)) Color.White else palette.penjasTint
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = opt,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = quiz.selectedOptionIndex != null,
                enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(200)),
                exit = shrinkVertically() + fadeOut(tween(150))
            ) {
                val isCorrect = quiz.selectedOptionIndex == question.correctIndex
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.subtleSurface)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = if (isCorrect) EmeraldAccent else HeartRose,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                if (isCorrect) {
                                    append("Benar! +${quiz.lastPointsEarned} poin 🎉 ")
                                } else {
                                    append("Belum tepat (+0 poin). ")
                                }
                            }
                            append(question.explanation)
                        },
                        fontSize = 12.sp,
                        color = palette.textPrimary,
                        lineHeight = 18.sp
                    )
                    if (question.fitnessTip.isNotBlank()) {
                        Text(
                            text = "💡 Tips Kebugaran: ${question.fitnessTip}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = palette.penjasTint,
                            lineHeight = 16.sp
                        )
                    }
                    val nextInteraction = remember { MutableInteractionSource() }
                    Button(
                        onClick = onNextQuestion,
                        interactionSource = nextInteraction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PenjasTealVibrant,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .pressScale(nextInteraction)
                            .testTag("quiz_next_button")
                    ) {
                        Text(
                            text = if (quiz.currentIndex < total - 1) "Soal Berikutnya →" else "Lihat Hasil & Skor",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizModePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    val pillBg by animateColorAsState(
        targetValue = if (selected) StravaOrange else Color.Transparent,
        animationSpec = tween(200),
        label = "quiz_mode_bg"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(pillBg)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else palette.textSecondary
        )
    }
}

@Composable
private fun QuizScoreStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(palette.subtleSurface)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontFamily = FredokaFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = palette.textSecondary
        )
    }
}

@Composable
private fun SmartWarmupRecommendationCard(
    plan: AdaptiveWarmupPlan,
    selectedExerciseId: Int,
    onSelectStretch: (Int) -> Unit,
    onStartRecommendedPackage: () -> Unit
) {
    val palette = LocalPulseRunColors.current
    val startInteraction = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(palette.cardBackground)
            .border(1.dp, PenjasTealVibrant.copy(alpha = 0.42f), RoundedCornerShape(24.dp))
            .padding(16.dp)
            .testTag("smart_warmup_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PenjasTealVibrant.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Rekomendasi pemanasan adaptif",
                        tint = palette.penjasTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "REKOMENDASI PEMANASAN CERDAS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.penjasTint,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = plan.planTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SunGold.copy(alpha = 0.18f))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${plan.totalDurationSeconds / 60} mnt",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CalorieAmber
                )
            }
        }

        // Context Pills: Current Weather + Recent Running History
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(palette.subtleSurface)
                    .border(1.dp, palette.cardBorder, CircleShape)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = plan.weatherSummaryChip,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(palette.subtleSurface)
                    .border(1.dp, palette.cardBorder, CircleShape)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = plan.historySummaryChip,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary
                )
            }
        }

        Text(
            text = plan.coachNote,
            fontSize = 12.sp,
            color = palette.textSecondary,
            lineHeight = 17.sp
        )

        // Interactive Recommended Stretches List
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            plan.recommendations.forEach { rec ->
                val isSelected = rec.exercise.id == selectedExerciseId
                val itemInteraction = remember(rec.exercise.id) { MutableInteractionSource() }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(itemInteraction, pressedScale = 0.985f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) PenjasTealVibrant.copy(alpha = 0.14f)
                            else palette.subtleSurface
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) PenjasTealVibrant else palette.cardBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable(
                            interactionSource = itemInteraction,
                            indication = LocalIndication.current,
                            onClick = { onSelectStretch(rec.exercise.id) }
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("recommended_stretch_${rec.exercise.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = rec.exercise.emoji, fontSize = 22.sp)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = rec.exercise.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(StravaOrange.copy(alpha = 0.14f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = rec.reasonTag,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StravaOrange
                                )
                            }
                        }
                        Text(
                            text = rec.whySuggested,
                            fontSize = 11.sp,
                            color = palette.textSecondary,
                            lineHeight = 15.sp
                        )
                    }
                    Text(
                        text = "${rec.exercise.durationSeconds}d",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.penjasTint
                    )
                }
            }
        }

        Button(
            onClick = onStartRecommendedPackage,
            interactionSource = startInteraction,
            colors = ButtonDefaults.buttonColors(
                containerColor = PenjasTealVibrant,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .pressScale(startInteraction)
                .testTag("btn_start_recommended_warmup")
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Mulai Paket Rekomendasi (${plan.recommendations.size} Gerakan)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

