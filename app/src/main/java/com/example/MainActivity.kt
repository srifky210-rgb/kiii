package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppThemeMode
import com.example.data.model.AchievementCategory
import com.example.notifications.ReminderScheduler
import com.example.ui.components.AchievementCelebrationDialog
import com.example.ui.components.ThemeSwitcherDialog
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PenjasScreen
import com.example.ui.screens.ProfileEditDialog
import com.example.ui.screens.ReminderSettingsDialog
import com.example.ui.screens.StravaScreen
import com.example.ui.theme.CalorieAmber
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.LocalPulseRunColors
import com.example.ui.theme.PenjasTealVibrant
import com.example.ui.theme.PulseRunTheme
import com.example.ui.theme.StravaOrange
import com.example.ui.theme.StravaOrangeDark
import com.example.ui.theme.SunGold
import com.example.ui.viewmodel.PulseRunViewModel
import com.example.ui.viewmodel.WorkoutStatus

const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_PENJAS = "penjas"
const val ROUTE_ACHIEVEMENTS = "achievements"
const val ROUTE_ANALYTICS = "analytics"
const val ROUTE_STRAVA = "strava"

private data class PulseNavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedColor: Color,
    val testTag: String
)

private val pulseNavDestinations = listOf(
    PulseNavDestination(
        route = ROUTE_DASHBOARD,
        label = "Hari Ini",
        icon = Icons.AutoMirrored.Rounded.DirectionsRun,
        selectedColor = StravaOrange,
        testTag = "nav_dashboard"
    ),
    PulseNavDestination(
        route = ROUTE_PENJAS,
        label = "Penjas",
        icon = Icons.Rounded.AccessibilityNew,
        selectedColor = PenjasTealVibrant,
        testTag = "nav_penjas"
    ),
    PulseNavDestination(
        route = ROUTE_ACHIEVEMENTS,
        label = "Lencana",
        icon = Icons.Rounded.EmojiEvents,
        selectedColor = SunGold,
        testTag = "nav_achievements"
    ),
    PulseNavDestination(
        route = ROUTE_ANALYTICS,
        label = "Analisis",
        icon = Icons.Rounded.Analytics,
        selectedColor = StravaOrange,
        testTag = "nav_analytics"
    ),
    PulseNavDestination(
        route = ROUTE_STRAVA,
        label = "Strava",
        icon = Icons.Rounded.Bolt,
        selectedColor = StravaOrange,
        testTag = "nav_strava"
    )
)

class MainActivity : ComponentActivity() {
    private var pendingNotificationTab by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingNotificationTab = intent?.getStringExtra(ReminderScheduler.EXTRA_TARGET_TAB)
        enableEdgeToEdge()
        setContent {
            val viewModel: PulseRunViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (uiState.preferences.themeMode) {
                AppThemeMode.SYSTEM -> systemDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            LaunchedEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    }
                )
            }

            PulseRunTheme(
                darkTheme = isDark,
                dynamicColor = uiState.preferences.useDynamicColor
            ) {
                val rootScreenKey = when {
                    !uiState.preferences.isLoaded -> 0
                    !uiState.preferences.isLoggedIn -> 1
                    else -> 2
                }
                AnimatedContent(
                    targetState = rootScreenKey,
                    transitionSpec = {
                        (fadeIn(tween(260)) + scaleIn(initialScale = 0.985f, animationSpec = tween(260)))
                            .togetherWith(fadeOut(tween(180)))
                    },
                    label = "root_screen_transition"
                ) { screenKey ->
                    when (screenKey) {
                        0 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background)
                            )
                        }
                        1 -> {
                            LoginScreen(
                                themeMode = uiState.preferences.themeMode,
                                isCurrentlyDark = isDark,
                                useDynamicColor = uiState.preferences.useDynamicColor,
                                onSelectThemeMode = { viewModel.setThemeMode(it) },
                                onToggleDynamicColor = { viewModel.setDynamicColor(it) },
                                onLogin = { name, email ->
                                    viewModel.loginAccount(name, email)
                                }
                            )
                        }
                        else -> {
                            PulseRunAppShell(
                                viewModel = viewModel,
                                isCurrentlyDark = isDark,
                                pendingNotificationTab = pendingNotificationTab,
                                onConsumeNotificationTab = { pendingNotificationTab = null }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val target = intent.getStringExtra(ReminderScheduler.EXTRA_TARGET_TAB)
        if (!target.isNullOrBlank()) {
            pendingNotificationTab = target
        }
    }
}

@Composable
fun PulseRunAppShell(
    viewModel: PulseRunViewModel,
    isCurrentlyDark: Boolean = true,
    pendingNotificationTab: String? = null,
    onConsumeNotificationTab: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_DASHBOARD
    val snackbarHostState = remember { SnackbarHostState() }
    var showProfileDialog by rememberSaveable { mutableStateOf(false) }
    var showReminderDialog by rememberSaveable { mutableStateOf(false) }
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var hasRequestedPermissions by rememberSaveable { mutableStateOf(false) }

    var pendingWorkoutStart by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    var hasNotificationPerm by remember {
        mutableStateOf(ReminderScheduler.hasNotificationPermission(context))
    }
    val runtimePermissions = remember {
        buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshLocationPermissions()
        hasNotificationPerm = ReminderScheduler.hasNotificationPermission(context)
        if (pendingWorkoutStart) {
            pendingWorkoutStart = false
            viewModel.toggleWorkout()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPerm = granted || ReminderScheduler.hasNotificationPermission(context)
        viewModel.showToast(
            if (hasNotificationPerm) "Izin notifikasi aktif"
            else "Izin notifikasi ditolak di pengaturan perangkat"
        )
    }

    fun handleStartOrToggleWorkout() {
        if (uiState.liveRun.status == WorkoutStatus.IDLE && !hasRequestedPermissions) {
            val missingAny = runtimePermissions.any { perm ->
                ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED
            }
            if (missingAny) {
                hasRequestedPermissions = true
                pendingWorkoutStart = true
                permissionLauncher.launch(runtimePermissions)
                return
            }
        }
        viewModel.toggleWorkout()
    }

    fun requestLocationPermissionsNow() {
        pendingWorkoutStart = false
        permissionLauncher.launch(runtimePermissions)
    }

    fun navigateTo(route: String) {
        if (currentRoute == route) return
        if (currentRoute == ROUTE_PENJAS) {
            viewModel.haltExercise()
        }
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    LaunchedEffect(pendingNotificationTab) {
        when (pendingNotificationTab) {
            ReminderScheduler.TAB_WEEKLY_QUIZ -> {
                viewModel.requestOpenWeeklyQuiz()
                navigateTo(ROUTE_PENJAS)
                onConsumeNotificationTab()
            }
            ReminderScheduler.TAB_DASHBOARD -> {
                navigateTo(ROUTE_DASHBOARD)
                onConsumeNotificationTab()
            }
        }
    }

    LaunchedEffect(uiState.toastMessage) {
        val message = uiState.toastMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissToast()
        }
    }

    val palette = LocalPulseRunColors.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    PulseRunTopBar(
                        athleteName = uiState.preferences.athleteName,
                        isSoundEnabled = uiState.preferences.isSoundEnabled,
                        themeMode = uiState.preferences.themeMode,
                        isStravaAutoSync = uiState.preferences.isStravaAutoSync,
                        hasRemindersEnabled = uiState.preferences.isRunReminderEnabled ||
                            uiState.preferences.isQuizReminderEnabled,
                        onOpenProfile = { showProfileDialog = true },
                        onOpenReminders = {
                            hasNotificationPerm = ReminderScheduler.hasNotificationPermission(context)
                            showReminderDialog = true
                        },
                        onToggleSound = { viewModel.toggleSoundEnabled() },
                        onOpenThemeSwitcher = { showThemeDialog = true },
                        onOpenStrava = { navigateTo(ROUTE_STRAVA) }
                    )
                },
                bottomBar = {
                    if (!useNavigationRail) {
                        Column {
                            HorizontalDivider(color = palette.cardBorder.copy(alpha = 0.75f))
                            NavigationBar(
                                containerColor = palette.cardBackground,
                                windowInsets = WindowInsets.navigationBars,
                                tonalElevation = 0.dp
                            ) {
                                pulseNavDestinations.forEach { dest ->
                                    val selected = currentRoute == dest.route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateTo(dest.route) },
                                        icon = {
                                            Icon(
                                                imageVector = dest.icon,
                                                contentDescription = dest.label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = dest.label,
                                                fontSize = 11.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = dest.selectedColor,
                                            selectedTextColor = dest.selectedColor,
                                            indicatorColor = dest.selectedColor.copy(alpha = 0.14f),
                                            unselectedIconColor = palette.textSecondary,
                                            unselectedTextColor = palette.textSecondary
                                        ),
                                        modifier = Modifier.testTag(dest.testTag)
                                    )
                                }
                            }
                        }
                    }
                },
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) { data ->
                        Snackbar(
                            snackbarData = data,
                            shape = RoundedCornerShape(16.dp),
                            containerColor = MaterialTheme.colorScheme.inverseSurface,
                            contentColor = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(
                            containerColor = palette.cardBackground,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Spacer(modifier = Modifier.height(12.dp))
                            pulseNavDestinations.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTo(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = dest.icon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = dest.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = dest.selectedColor,
                                        selectedTextColor = dest.selectedColor,
                                        indicatorColor = dest.selectedColor.copy(alpha = 0.14f),
                                        unselectedIconColor = palette.textSecondary,
                                        unselectedTextColor = palette.textSecondary
                                    ),
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_DASHBOARD,
                        enterTransition = {
                            fadeIn(tween(200)) + scaleIn(initialScale = 0.985f, animationSpec = tween(220))
                        },
                        exitTransition = {
                            fadeOut(tween(150))
                        },
                        popEnterTransition = {
                            fadeIn(tween(200)) + scaleIn(initialScale = 0.985f, animationSpec = tween(220))
                        },
                        popExitTransition = {
                            fadeOut(tween(150))
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        composable(ROUTE_DASHBOARD) {
                            DashboardScreen(
                                uiState = uiState,
                                heartWaveFlow = viewModel.heartWave,
                                onToggleWorkout = { handleStartOrToggleWorkout() },
                                onFinishWorkout = { viewModel.finishAndSaveWorkout() },
                                onRequestLocationPermission = { requestLocationPermissionsNow() },
                                onNavigateToPenjas = { navigateTo(ROUTE_PENJAS) },
                                onOpenWeeklyQuiz = {
                                    viewModel.requestOpenWeeklyQuiz()
                                    navigateTo(ROUTE_PENJAS)
                                },
                                onOpenAchievements = { navigateTo(ROUTE_ACHIEVEMENTS) },
                                onRefreshWeather = { viewModel.refreshWeather(force = true) }
                            )
                        }
                        composable(ROUTE_PENJAS) {
                            PenjasScreen(
                                uiState = uiState,
                                onSelectExercise = { viewModel.selectExercise(it) },
                                onToggleVideoDemo = { viewModel.toggleVideoDemo() },
                                onGoDemoStep = { viewModel.goDemoStep(it) },
                                onToggleDemoPause = { viewModel.toggleDemoPause() },
                                onToggleSingleExercise = { viewModel.toggleSingleExercise() },
                                onSpeakExercise = { viewModel.speakCurrentExercise() },
                                onStartFullSession = { viewModel.startFullMorningSession() },
                                onHaltExercise = { viewModel.haltExercise() },
                                onSelectQuizMode = { viewModel.selectQuizMode(it) },
                                onSelectWeeklyTheme = { viewModel.selectWeeklyTheme(it) },
                                onSpeakQuizQuestion = { viewModel.speakCurrentQuizQuestion() },
                                onAnswerQuizOption = { viewModel.answerQuizOption(it) },
                                onNextQuizQuestion = { viewModel.nextQuizQuestion() },
                                onRestartQuiz = { viewModel.restartQuiz() },
                                onStartRecommendedWarmup = { viewModel.startRecommendedWarmupSession() }
                            )
                        }
                        composable(ROUTE_ACHIEVEMENTS) {
                            AchievementsScreen(
                                uiState = uiState,
                                onNavigateForCategory = { category ->
                                    when (category) {
                                        AchievementCategory.QUIZ -> {
                                            viewModel.requestOpenWeeklyQuiz()
                                            navigateTo(ROUTE_PENJAS)
                                        }
                                        AchievementCategory.MILESTONE -> navigateTo(ROUTE_PENJAS)
                                        else -> navigateTo(ROUTE_DASHBOARD)
                                    }
                                }
                            )
                        }
                        composable(ROUTE_ANALYTICS) {
                            AnalyticsScreen(
                                uiState = uiState,
                                onResetPenjasProgress = { viewModel.resetPenjasProgress() }
                            )
                        }
                        composable(ROUTE_STRAVA) {
                            StravaScreen(
                                uiState = uiState,
                                onToggleAutoSync = { viewModel.setStravaAutoSync(it) },
                                onToggleWorkoutSync = { id, synced -> viewModel.toggleWorkoutSync(id, synced) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showProfileDialog) {
        ProfileEditDialog(
            initialName = uiState.preferences.athleteName,
            accountEmail = uiState.preferences.accountEmail,
            initialStepGoal = uiState.preferences.stepGoal,
            initialCalorieGoal = uiState.preferences.calorieGoal,
            themeMode = uiState.preferences.themeMode,
            onSelectThemeMode = { viewModel.setThemeMode(it) },
            onDismiss = { showProfileDialog = false },
            onSave = { name, stepGoal, calorieGoal ->
                viewModel.updateProfile(name, stepGoal, calorieGoal)
            },
            onLogout = { viewModel.logoutAccount() }
        )
    }

    if (showReminderDialog) {
        ReminderSettingsDialog(
            preferences = uiState.preferences,
            hasNotificationPermission = hasNotificationPerm,
            onRequestNotificationPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    hasNotificationPerm = true
                }
            },
            onUpdateRunReminder = { enabled, hour, minute ->
                viewModel.updateRunReminder(enabled, hour, minute)
            },
            onUpdateQuizReminder = { enabled, dayOfWeek, hour, minute ->
                viewModel.updateQuizReminder(enabled, dayOfWeek, hour, minute)
            },
            onSendTestRunNotification = { viewModel.sendTestRunNotification() },
            onSendTestQuizNotification = { viewModel.sendTestQuizNotification() },
            onDismiss = { showReminderDialog = false }
        )
    }

    if (showThemeDialog) {
        ThemeSwitcherDialog(
            themeMode = uiState.preferences.themeMode,
            isCurrentlyDark = isCurrentlyDark,
            useDynamicColor = uiState.preferences.useDynamicColor,
            onSelectThemeMode = { viewModel.setThemeMode(it) },
            onToggleDynamicColor = { viewModel.setDynamicColor(it) },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (uiState.newlyUnlockedBadges.isNotEmpty()) {
        AchievementCelebrationDialog(
            badges = uiState.newlyUnlockedBadges,
            onViewBadges = { navigateTo(ROUTE_ACHIEVEMENTS) },
            onDismiss = { viewModel.dismissNewBadgesCelebration() }
        )
    }
}

@Composable
private fun PulseRunTopBar(
    athleteName: String,
    isSoundEnabled: Boolean,
    themeMode: AppThemeMode,
    isStravaAutoSync: Boolean,
    hasRemindersEnabled: Boolean,
    onOpenProfile: () -> Unit,
    onOpenReminders: () -> Unit,
    onToggleSound: () -> Unit,
    onOpenThemeSwitcher: () -> Unit,
    onOpenStrava: () -> Unit
) {
    val palette = LocalPulseRunColors.current
    val initials = remember(athleteName) {
        athleteName.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifEmpty { "PR" }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.cardBackground.copy(alpha = 0.96f))
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Athlete profile button (min 48dp height touch target)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenProfile)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .testTag("profile_edit_trigger"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(listOf(StravaOrange, StravaOrangeDark))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Semangat Pagi,",
                        fontSize = 11.sp,
                        color = palette.textSecondary,
                        maxLines = 1
                    )
                    Text(
                        text = athleteName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                IconButton(
                    onClick = onOpenReminders,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("notifications_button")
                ) {
                    Icon(
                        imageVector = if (hasRemindersEnabled) {
                            Icons.Rounded.NotificationsActive
                        } else {
                            Icons.Rounded.NotificationsNone
                        },
                        contentDescription = "Atur pengingat notifikasi",
                        tint = if (hasRemindersEnabled) StravaOrange else palette.textSecondary
                    )
                }

                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("sound_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isSoundEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                        contentDescription = if (isSoundEnabled) "Matikan suara panduan" else "Aktifkan suara panduan",
                        tint = if (isSoundEnabled) PenjasTealVibrant else palette.textSecondary
                    )
                }

                IconButton(
                    onClick = onOpenThemeSwitcher,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("theme_toggle_button")
                ) {
                    val themeIcon = when (themeMode) {
                        AppThemeMode.SYSTEM -> Icons.Rounded.BrightnessAuto
                        AppThemeMode.LIGHT -> Icons.Rounded.LightMode
                        AppThemeMode.DARK -> Icons.Rounded.DarkMode
                    }
                    Icon(
                        imageVector = themeIcon,
                        contentDescription = "Pengalih tema tampilan (${themeMode.label})",
                        tint = if (themeMode == AppThemeMode.SYSTEM) {
                            palette.textSecondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }

                val statusTint = if (isStravaAutoSync) EmeraldAccent else CalorieAmber
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenStrava)
                        .padding(horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(statusTint.copy(alpha = 0.14f))
                            .border(1.dp, statusTint.copy(alpha = 0.35f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("strava_status_chip"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(statusTint)
                        )
                        Text(
                            text = if (isStravaAutoSync) "Synced" else "Paused",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusTint
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = palette.cardBorder.copy(alpha = 0.75f))
    }
}
