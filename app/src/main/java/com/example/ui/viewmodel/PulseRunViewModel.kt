package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioCoachManager
import com.example.data.local.AchievementUnlockEntity
import com.example.data.local.AppThemeMode
import com.example.data.local.DailyMetricEntity
import com.example.data.local.PenjasExerciseLogEntity
import com.example.data.local.QuizResultEntity
import com.example.data.local.UserPreferences
import com.example.data.local.WorkoutActivityEntity
import com.example.data.model.AchievementCatalog
import com.example.data.model.AchievementDefinition
import com.example.data.model.AchievementSummary
import com.example.data.model.AdaptiveWarmupPlan
import com.example.data.model.PenjasCatalog
import com.example.data.model.PenjasExercise
import com.example.data.model.PenjasQuizQuestion
import com.example.data.model.RoutePoint
import com.example.data.model.WarmupRecommender
import com.example.data.model.WeeklyQuizCatalog
import com.example.data.model.WeeklyQuizTheme
import com.example.data.remote.LocalWeatherState
import com.example.data.remote.OpenMeteoWeatherRepository
import com.example.data.repository.PulseRunRepository
import com.example.notifications.ReminderScheduler
import com.example.sensors.FitnessSensorManager
import com.example.sensors.RunningTrackerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

enum class WorkoutStatus {
    IDLE, ACTIVE, PAUSED
}

data class LiveRunState(
    val status: WorkoutStatus = WorkoutStatus.IDLE,
    val elapsedSeconds: Long = 0L,
    val sessionSteps: Int = 0,
    val sessionDistanceKm: Double = 0.0,
    val sessionCalories: Int = 0,
    val paceDisplay: String = "0'00\"",
    val currentPaceDisplay: String = "0'00\"",
    val currentSpeedKmh: Double = 0.0,
    val gpsAccuracyMeters: Float? = null,
    val elevationGainMeters: Double = 0.0,
    val gpsPointCount: Int = 0,
    val currentBpm: Int = 78,
    val minBpm: Int = 64,
    val avgBpm: Int = 82,
    val maxBpm: Int = 145,
    val bpmWaveform: List<Float> = List(45) { 78f },
    val hasGpsFix: Boolean = false,
    val hasGpsPermission: Boolean = false,
    val hasHardwareSteps: Boolean = false,
    val routePoints: List<RoutePoint> = emptyList()
)

data class HeartWaveState(
    val currentBpm: Int = 78,
    val waveform: List<Float> = List(45) { 78f }
)

data class PenjasPlayerState(
    val selectedExerciseIndex: Int = 0,
    val isExerciseRunning: Boolean = false,
    val isAutoSession: Boolean = false,
    val isRestPhase: Boolean = false,
    val timeLeftSeconds: Int = PenjasCatalog.exercises.first().durationSeconds,
    val totalTimeSeconds: Int = PenjasCatalog.exercises.first().durationSeconds,
    val statusText: String = "Siap",
    val isDemoActive: Boolean = false,
    val isDemoPaused: Boolean = false,
    val demoStepIndex: Int = 0,
    val demoTotalSteps: Int = 1,
    val demoCaption: String = DEFAULT_DEMO_CAPTION,
    val highlightedStepIndex: Int = -1,
    val activeQueueLabel: String = ""
) {
    val selectedExercise: PenjasExercise
        get() = PenjasCatalog.findExercise(selectedExerciseIndex)

    val isAnimationPlaying: Boolean
        get() = (isExerciseRunning && !isRestPhase) || (isDemoActive && !isDemoPaused)
}

enum class QuizMode(val storageKey: String) {
    WEEKLY("WEEKLY"),
    PENJAS_BASIC("PENJAS_BASIC")
}

data class QuizUiState(
    val mode: QuizMode = QuizMode.WEEKLY,
    val selectedWeekIndex: Int = 1,
    val currentIndex: Int = 0,
    val score: Int = 0,
    val points: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastPointsEarned: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isFinished: Boolean = false
) {
    val activeWeeklyTheme: WeeklyQuizTheme
        get() = WeeklyQuizCatalog.themeForWeek(selectedWeekIndex)

    val questions: List<PenjasQuizQuestion>
        get() = if (mode == QuizMode.WEEKLY) activeWeeklyTheme.questions else PenjasCatalog.quizQuestions

    val activeTitle: String
        get() = if (mode == QuizMode.WEEKLY) {
            "${activeWeeklyTheme.weekLabel}: ${activeWeeklyTheme.title}"
        } else {
            "Kuis Dasar Penjas"
        }
}

data class PulseRunUiState(
    val preferences: UserPreferences = UserPreferences(),
    val liveRun: LiveRunState = LiveRunState(),
    val penjas: PenjasPlayerState = PenjasPlayerState(),
    val quiz: QuizUiState = QuizUiState(),
    val workouts: List<WorkoutActivityEntity> = emptyList(),
    val penjasLogs: List<PenjasExerciseLogEntity> = emptyList(),
    val quizResults: List<QuizResultEntity> = emptyList(),
    val dailyMetrics: List<DailyMetricEntity> = emptyList(),
    val todayDateKey: String = "",
    val currentWeekKey: String = "",
    val activeCycleWeekIndex: Int = 1,
    val penjasQuizTabTrigger: Int = 0,
    val streakDays: Int = 0,
    val achievements: AchievementSummary = AchievementSummary(),
    val newlyUnlockedBadges: List<AchievementDefinition> = emptyList(),
    val weather: LocalWeatherState = LocalWeatherState(),
    val warmupPlan: AdaptiveWarmupPlan = WarmupRecommender.recommend(LocalWeatherState(), emptyList()),
    val toastMessage: String? = null
)

private const val DEFAULT_DEMO_CAPTION =
    "Tekan “Video Demo” untuk melihat contoh gerakan lengkap dengan penjelasan suara."

class PulseRunViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PulseRunRepository(application)
    private val weatherRepository = OpenMeteoWeatherRepository(application)
    private val audioCoach = AudioCoachManager(application)
    private val sensorManager = FitnessSensorManager(application)

    private val _liveRun = MutableStateFlow(
        LiveRunState(
            hasGpsPermission = sensorManager.checkLocationPermission(),
            hasHardwareSteps = sensorManager.snapshot.value.hasHardwareStepSensor
        )
    )
    private val _heartWave = MutableStateFlow(HeartWaveState())
    val heartWave: StateFlow<HeartWaveState> = _heartWave.asStateFlow()
    private val _penjas = MutableStateFlow(PenjasPlayerState())
    private val _quiz = MutableStateFlow(
        QuizUiState(selectedWeekIndex = repository.currentCycleWeekIndex())
    )
    private val _penjasQuizTabTrigger = MutableStateFlow(0)
    private val _newlyUnlockedBadges = MutableStateFlow<List<AchievementDefinition>>(emptyList())
    private val _weather = MutableStateFlow(LocalWeatherState(isLoading = true))
    private val _toastMessage = MutableStateFlow<String?>(null)
    private var hasSyncedInitialAchievements = false

    private var workoutTimerJob: Job? = null
    private var exerciseTimerJob: Job? = null
    private var exerciseBeatJob: Job? = null
    private var demoStepJob: Job? = null
    private var demoQueue: List<String> = emptyList()
    private var bpmSampleSum = 82L
    private var bpmSampleCount = 1L

    private data class UiSignals(
        val toast: String?,
        val quizTrigger: Int,
        val unlocks: List<AchievementUnlockEntity>,
        val newBadges: List<AchievementDefinition>,
        val weather: LocalWeatherState
    )

    private data class PersistedBundle(
        val workouts: List<WorkoutActivityEntity>,
        val logs: List<PenjasExerciseLogEntity>,
        val quizzes: List<QuizResultEntity>,
        val metrics: List<DailyMetricEntity>,
        val signals: UiSignals
    )

    val uiState: StateFlow<PulseRunUiState> = combine(
        repository.userPreferencesFlow,
        _liveRun,
        _penjas,
        _quiz,
        combine(
            repository.workoutsFlow,
            repository.penjasLogsFlow,
            repository.quizResultsFlow,
            repository.dailyMetricsFlow,
            combine(
                _toastMessage,
                _penjasQuizTabTrigger,
                repository.achievementUnlocksFlow,
                _newlyUnlockedBadges,
                _weather
            ) { toast, quizTrigger, unlocks, newBadges, weatherState ->
                UiSignals(toast, quizTrigger, unlocks, newBadges, weatherState)
            }
        ) { workouts, logs, quizzes, metrics, signals ->
            PersistedBundle(workouts, logs, quizzes, metrics, signals)
        }
    ) { prefs, liveRun, penjas, quiz, bundle ->
        audioCoach.isSoundEnabled = prefs.isSoundEnabled
        val summary = AchievementCatalog.evaluate(
            workouts = bundle.workouts,
            penjasLogs = bundle.logs,
            quizResults = bundle.quizzes,
            persistedUnlocks = bundle.signals.unlocks
        )
        PulseRunUiState(
            preferences = prefs,
            liveRun = liveRun,
            penjas = penjas,
            quiz = quiz,
            workouts = bundle.workouts,
            penjasLogs = bundle.logs,
            quizResults = bundle.quizzes,
            dailyMetrics = bundle.metrics,
            todayDateKey = repository.todayDateKey(),
            currentWeekKey = repository.currentWeekKey(),
            activeCycleWeekIndex = repository.currentCycleWeekIndex(),
            penjasQuizTabTrigger = bundle.signals.quizTrigger,
            streakDays = summary.activeStreakDays,
            achievements = summary,
            newlyUnlockedBadges = bundle.signals.newBadges,
            weather = bundle.signals.weather,
            warmupPlan = WarmupRecommender.recommend(bundle.signals.weather, bundle.workouts),
            toastMessage = bundle.signals.toast
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = PulseRunUiState(
            todayDateKey = repository.todayDateKey(),
            currentWeekKey = repository.currentWeekKey(),
            activeCycleWeekIndex = repository.currentCycleWeekIndex(),
            achievements = AchievementCatalog.evaluate(
                workouts = emptyList(),
                penjasLogs = emptyList(),
                quizResults = emptyList(),
                persistedUnlocks = emptyList()
            ),
            warmupPlan = WarmupRecommender.recommend(LocalWeatherState(), emptyList())
        )
    )

    init {
        startHeartRateWaveformLoop()
        observeSensorUpdates()
        observeReminderSync()
        observeAchievementUnlocks()
        refreshWeather(force = true)
    }

    fun refreshWeather(force: Boolean = false) {
        val now = System.currentTimeMillis()
        val current = _weather.value
        if (!force && current.hasLoadedData && (now - current.lastUpdatedMillis) < 15 * 60_000L) {
            return
        }
        viewModelScope.launch {
            _weather.update { it.copy(isLoading = true, errorMessage = null) }
            val coords = sensorManager.getBestKnownCoordinates()
            weatherRepository.fetchLocalWeather(coords)
                .onSuccess { fresh ->
                    _weather.value = fresh
                }
                .onFailure {
                    _weather.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Gagal memuat cuaca. Periksa koneksi internet."
                        )
                    }
                }
        }
    }

    private fun observeAchievementUnlocks() {
        viewModelScope.launch {
            combine(
                repository.workoutsFlow,
                repository.penjasLogsFlow,
                repository.quizResultsFlow,
                repository.achievementUnlocksFlow,
                repository.userPreferencesFlow
            ) { workouts, logs, quizzes, persisted, prefs ->
                Triple(
                    AchievementCatalog.evaluate(workouts, logs, quizzes, persisted),
                    persisted,
                    prefs
                )
            }.collect { (summary, persisted, prefs) ->
                val newPairs = AchievementCatalog.findNewlyEarnedUnlocks(summary, persisted)
                if (newPairs.isNotEmpty()) {
                    repository.recordAchievementUnlocks(newPairs.map { it.second })
                    if (hasSyncedInitialAchievements && prefs.isLoggedIn) {
                        val defs = newPairs.map { it.first }
                        _newlyUnlockedBadges.update { current ->
                            (current + defs).distinctBy { it.id }
                        }
                        audioCoach.sfxFanfare()
                    }
                }
                if (prefs.isLoaded) {
                    hasSyncedInitialAchievements = true
                }
            }
        }
    }

    fun dismissNewBadgesCelebration() {
        _newlyUnlockedBadges.value = emptyList()
    }

    private fun observeReminderSync() {
        viewModelScope.launch {
            repository.userPreferencesFlow.collect { prefs ->
                if (prefs.isLoaded && prefs.isLoggedIn) {
                    ReminderScheduler.syncSchedules(
                        context = getApplication(),
                        athleteName = prefs.athleteName,
                        runEnabled = prefs.isRunReminderEnabled,
                        runHour = prefs.runReminderHour,
                        runMinute = prefs.runReminderMinute,
                        quizEnabled = prefs.isQuizReminderEnabled,
                        quizDayOfWeek = prefs.quizReminderDayOfWeek,
                        quizHour = prefs.quizReminderHour,
                        quizMinute = prefs.quizReminderMinute
                    )
                }
            }
        }
    }

    private fun observeSensorUpdates() {
        viewModelScope.launch {
            sensorManager.snapshot.collect { snap ->
                _liveRun.update { state ->
                    val liveSpeedKmh = if (state.status == WorkoutStatus.ACTIVE) {
                        snap.currentSpeedMps * 3.6
                    } else {
                        0.0
                    }
                    state.copy(
                        hasGpsPermission = snap.hasGpsPermission,
                        hasGpsFix = snap.hasGpsSignal,
                        gpsAccuracyMeters = snap.gpsAccuracyMeters,
                        currentSpeedKmh = liveSpeedKmh,
                        hasHardwareSteps = snap.hasHardwareStepSensor,
                        elevationGainMeters = snap.elevationGainMeters,
                        gpsPointCount = snap.recordedCoordinates.size
                    )
                }
            }
        }
    }

    fun refreshLocationPermissions() {
        sensorManager.refreshPermissionState()
        if (_liveRun.value.status == WorkoutStatus.ACTIVE && sensorManager.checkLocationPermission()) {
            sensorManager.startTracking()
        }
        refreshWeather(force = true)
    }

    private fun startHeartRateWaveformLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(450L)
                val runState = _liveRun.value
                val baseBpm = when {
                    runState.status == WorkoutStatus.ACTIVE -> runState.currentBpm
                    _penjas.value.isAnimationPlaying -> 108 + Random.nextInt(-3, 5)
                    else -> 76 + Random.nextInt(0, 6)
                }
                val spike = if (Random.nextFloat() > 0.75f) baseBpm * 0.24f else Random.nextFloat() * 6f - 3f
                _heartWave.update { wave ->
                    wave.copy(
                        currentBpm = baseBpm,
                        waveform = (wave.waveform + (baseBpm + spike)).takeLast(45)
                    )
                }
            }
        }
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun dismissToast() {
        _toastMessage.value = null
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
            val label = when (mode) {
                AppThemeMode.SYSTEM -> "Tema mengikuti pengaturan sistem"
                AppThemeMode.LIGHT -> "Mode Terang aktif"
                AppThemeMode.DARK -> "Mode Gelap aktif"
            }
            showToast(label)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDynamicColor(enabled)
            showToast(if (enabled) "Warna dinamis Material 3 aktif" else "Warna khas PulseRun aktif")
        }
    }

    fun toggleSoundEnabled() {
        viewModelScope.launch {
            val current = uiState.value.preferences.isSoundEnabled
            repository.toggleSoundEnabled(current)
            if (current) {
                audioCoach.stopSpeaking()
                showToast("Mode senyap aktif")
            } else {
                showToast("Suara panduan & audio aktif")
            }
        }
    }

    fun setStravaAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            repository.setStravaAutoSync(enabled)
            showToast(if (enabled) "Sinkronisasi otomatis Strava aktif" else "Sinkronisasi Strava dijeda")
        }
    }

    fun toggleWorkoutSync(workoutId: Long, currentSynced: Boolean) {
        viewModelScope.launch {
            repository.toggleWorkoutSync(workoutId, !currentSynced)
            showToast(if (!currentSynced) "Aktivitas disinkronkan ke Strava" else "Status sinkronisasi dijeda")
        }
    }

    fun loginAccount(accountName: String, accountEmail: String) {
        val cleanName = accountName.trim()
        if (cleanName.isEmpty()) return
        viewModelScope.launch {
            repository.loginAccount(cleanName, accountEmail)
            showToast("Selamat datang, $cleanName!")
        }
    }

    fun logoutAccount() {
        haltExercise()
        if (_liveRun.value.status != WorkoutStatus.IDLE) {
            workoutTimerJob?.cancel()
            sensorManager.stopTracking()
            RunningTrackerService.stop(getApplication())
            _liveRun.update {
                it.copy(
                    status = WorkoutStatus.IDLE,
                    elapsedSeconds = 0L,
                    sessionSteps = 0,
                    sessionDistanceKm = 0.0,
                    sessionCalories = 0,
                    paceDisplay = "0'00\"",
                    currentPaceDisplay = "0'00\"",
                    currentSpeedKmh = 0.0,
                    elevationGainMeters = 0.0,
                    gpsPointCount = 0
                )
            }
        }
        viewModelScope.launch {
            repository.logoutAccount()
        }
    }

    fun requestOpenWeeklyQuiz() {
        _quiz.value = QuizUiState(
            mode = QuizMode.WEEKLY,
            selectedWeekIndex = repository.currentCycleWeekIndex()
        )
        _penjasQuizTabTrigger.value = _penjasQuizTabTrigger.value + 1
    }

    fun updateProfile(name: String, stepGoal: Int, calorieGoal: Int) {
        viewModelScope.launch {
            repository.updateProfile(name, stepGoal, calorieGoal)
            showToast("Profil & target harian diperbarui")
        }
    }

    fun updateRunReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.updateRunReminder(enabled, hour, minute)
            showToast(
                if (enabled) String.format(Locale.US, "Pengingat lari diatur pukul %02d:%02d", hour, minute)
                else "Pengingat lari harian dinonaktifkan"
            )
        }
    }

    fun updateQuizReminder(enabled: Boolean, dayOfWeek: Int, hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.updateQuizReminder(enabled, dayOfWeek, hour, minute)
            showToast(
                if (enabled) "Pengingat kuis mingguan diaktifkan"
                else "Pengingat kuis mingguan dinonaktifkan"
            )
        }
    }

    fun sendTestRunNotification() {
        val app = getApplication<Application>()
        if (!ReminderScheduler.hasNotificationPermission(app)) {
            showToast("Aktifkan izin notifikasi terlebih dahulu")
            return
        }
        ReminderScheduler.showRunNotification(app, uiState.value.preferences.athleteName)
        showToast("Notifikasi pengingat lari dikirim!")
    }

    fun sendTestQuizNotification() {
        val app = getApplication<Application>()
        if (!ReminderScheduler.hasNotificationPermission(app)) {
            showToast("Aktifkan izin notifikasi terlebih dahulu")
            return
        }
        ReminderScheduler.showWeeklyQuizNotification(app, uiState.value.preferences.athleteName)
        showToast("Notifikasi kuis mingguan dikirim!")
    }

    // ==================== LIVE WORKOUT TRACKING ====================

    fun toggleWorkout() {
        val currentStatus = _liveRun.value.status
        when (currentStatus) {
            WorkoutStatus.IDLE -> startWorkout()
            WorkoutStatus.ACTIVE -> pauseWorkout()
            WorkoutStatus.PAUSED -> resumeWorkout()
        }
    }

    private var lastAnnouncedKm = 0
    private var cadenceAccSteps = 0

    private fun startWorkout() {
        audioCoach.vibrateShort()
        lastAnnouncedKm = 0
        cadenceAccSteps = 0
        bpmSampleSum = 82L
        bpmSampleCount = 1L
        sensorManager.startTracking()
        _liveRun.update {
            it.copy(
                status = WorkoutStatus.ACTIVE,
                elapsedSeconds = 0L,
                sessionSteps = 0,
                sessionDistanceKm = 0.0,
                sessionCalories = 0,
                paceDisplay = "0'00\"",
                currentPaceDisplay = "0'00\"",
                currentSpeedKmh = 0.0,
                elevationGainMeters = 0.0,
                gpsPointCount = 0,
                routePoints = listOf(RoutePoint(0.10f, 0.75f))
            )
        }
        if (sensorManager.checkLocationPermission()) {
            RunningTrackerService.updateNotification(
                context = getApplication(),
                elapsed = "00:00:00",
                distance = "0.00 km",
                pace = "0'00\"/km",
                paused = false,
                isStart = true
            )
        }
        startWorkoutTimerLoop()
        showToast(
            if (sensorManager.checkLocationPermission()) "Pelacakan GPS & Pace real-time dimulai"
            else "Pelacakan lari dimulai (mode kadens)"
        )
    }

    private fun resumeWorkout() {
        audioCoach.vibrateShort()
        sensorManager.resumeTracking()
        _liveRun.update { it.copy(status = WorkoutStatus.ACTIVE) }
        val run = _liveRun.value
        if (sensorManager.checkLocationPermission()) {
            RunningTrackerService.updateNotification(
                context = getApplication(),
                elapsed = formatElapsed(run.elapsedSeconds),
                distance = String.format(Locale.US, "%.2f km", run.sessionDistanceKm),
                pace = "${run.paceDisplay}/km",
                paused = false
            )
        }
        startWorkoutTimerLoop()
        showToast("Sesi lari dilanjutkan")
    }

    private fun pauseWorkout() {
        workoutTimerJob?.cancel()
        sensorManager.pauseTracking()
        _liveRun.update {
            it.copy(
                status = WorkoutStatus.PAUSED,
                currentSpeedKmh = 0.0,
                currentPaceDisplay = "0'00\""
            )
        }
        val run = _liveRun.value
        if (sensorManager.checkLocationPermission()) {
            RunningTrackerService.updateNotification(
                context = getApplication(),
                elapsed = formatElapsed(run.elapsedSeconds),
                distance = String.format(Locale.US, "%.2f km", run.sessionDistanceKm),
                pace = "${run.paceDisplay}/km",
                paused = true
            )
        }
        showToast("Sesi lari dijeda")
    }

    private fun formatElapsed(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
    }

    private fun formatPace(secondsPerKm: Int): String {
        val clamped = secondsPerKm.coerceIn(150, 1200)
        return "${clamped / 60}'${(clamped % 60).toString().padStart(2, '0')}\""
    }

    private fun startWorkoutTimerLoop() {
        workoutTimerJob?.cancel()
        workoutTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val snap = sensorManager.snapshot.value
                var notifyTime = ""
                var notifyDist = ""
                var notifyPace = ""

                _liveRun.update { state ->
                    val nextElapsed = state.elapsedSeconds + 1L
                    val hardwareSteps = snap.sessionHardwareSteps
                    val nextSteps = if (snap.hasHardwareStepSensor && hardwareSteps > 0) {
                        hardwareSteps
                    } else if (snap.hasGpsSignal && snap.sessionGpsDistanceKm > 0.005) {
                        (snap.sessionGpsDistanceKm / 0.00075).roundToInt()
                    } else {
                        cadenceAccSteps += Random.nextInt(2, 4)
                        cadenceAccSteps
                    }

                    // Prefer real GPS distance when GPS signal is locked; fallback to cadence estimate only without GPS
                    val nextDist = if (snap.hasGpsSignal) {
                        snap.sessionGpsDistanceKm
                    } else {
                        nextSteps * 0.00075
                    }

                    val nextCalories = maxOf((nextDist * 62.0).roundToInt(), (nextSteps * 0.045).roundToInt())
                    val speedKmh = if (snap.hasGpsSignal && snap.currentSpeedMps > 0.3f) {
                        snap.currentSpeedMps * 3.6
                    } else if (nextElapsed > 0L && nextDist > 0.005) {
                        (nextDist / (nextElapsed / 3600.0)).coerceIn(0.0, 28.0)
                    } else {
                        0.0
                    }

                    val avgPaceStr = if (nextDist >= 0.015) {
                        formatPace((nextElapsed / nextDist).toInt())
                    } else {
                        "0'00\""
                    }

                    val instantPaceStr = if (snap.hasGpsSignal && snap.currentSpeedMps >= 0.6f) {
                        formatPace((1000f / snap.currentSpeedMps).toInt())
                    } else {
                        avgPaceStr
                    }

                    val targetBpm = when {
                        speedKmh >= 10.0 -> 158 + Random.nextInt(-5, 7)
                        speedKmh >= 6.0 -> 144 + Random.nextInt(-6, 7)
                        else -> 128 + Random.nextInt(-5, 6)
                    }
                    val smoothedBpm = (state.currentBpm + (targetBpm - state.currentBpm) * 0.25f).roundToInt()
                    bpmSampleSum += smoothedBpm
                    bpmSampleCount++
                    val nextMax = maxOf(state.maxBpm, smoothedBpm)
                    val nextMin = minOf(state.minBpm, smoothedBpm)
                    val nextAvg = (bpmSampleSum / bpmSampleCount).toInt()
                    val updatedRoute = normalizeRoutePoints(snap.recordedCoordinates, nextElapsed)

                    val wholeKm = nextDist.toInt()
                    if (wholeKm > lastAnnouncedKm && wholeKm >= 1) {
                        lastAnnouncedKm = wholeKm
                        audioCoach.speak("Jarak $wholeKm kilometer selesai. Pace rata-rata $avgPaceStr per kilometer.")
                    }

                    notifyTime = formatElapsed(nextElapsed)
                    notifyDist = String.format(Locale.US, "%.2f km", nextDist)
                    notifyPace = "$avgPaceStr/km"

                    state.copy(
                        elapsedSeconds = nextElapsed,
                        sessionSteps = nextSteps,
                        sessionDistanceKm = nextDist,
                        sessionCalories = nextCalories,
                        paceDisplay = avgPaceStr,
                        currentPaceDisplay = instantPaceStr,
                        currentSpeedKmh = speedKmh,
                        gpsAccuracyMeters = snap.gpsAccuracyMeters,
                        elevationGainMeters = snap.elevationGainMeters,
                        gpsPointCount = snap.recordedCoordinates.size,
                        currentBpm = smoothedBpm,
                        minBpm = nextMin,
                        avgBpm = nextAvg,
                        maxBpm = nextMax,
                        routePoints = updatedRoute
                    )
                }

                if (sensorManager.checkLocationPermission()) {
                    RunningTrackerService.updateNotification(
                        context = getApplication(),
                        elapsed = notifyTime,
                        distance = notifyDist,
                        pace = notifyPace,
                        paused = false
                    )
                }
            }
        }
    }

    private fun normalizeRoutePoints(coords: List<Pair<Double, Double>>, elapsed: Long): List<RoutePoint> {
        if (coords.size >= 2) {
            val minLat = coords.minOf { it.first }
            val maxLat = coords.maxOf { it.first }
            val minLng = coords.minOf { it.second }
            val maxLng = coords.maxOf { it.second }
            val latSpan = (maxLat - minLat).coerceAtLeast(0.0005)
            val lngSpan = (maxLng - minLng).coerceAtLeast(0.0005)
            return coords.map { (lat, lng) ->
                val x = (0.10 + ((lng - minLng) / lngSpan) * 0.80).toFloat()
                val y = (0.85 - ((lat - minLat) / latSpan) * 0.65).toFloat()
                RoutePoint(x, y)
            }
        }
        val progress = (elapsed.coerceAtMost(60) / 60f)
        val base = listOf(
            RoutePoint(0.10f, 0.73f),
            RoutePoint(0.25f, 0.25f),
            RoutePoint(0.45f, 0.50f),
            RoutePoint(0.72f, 0.30f),
            RoutePoint(0.90f, 0.65f)
        )
        val count = (2 + (progress * 3).toInt()).coerceIn(2, base.size)
        return base.take(count)
    }

    fun finishAndSaveWorkout() {
        workoutTimerJob?.cancel()
        sensorManager.stopTracking()
        RunningTrackerService.stop(getApplication())
        val run = _liveRun.value
        if (run.elapsedSeconds == 0L && run.sessionSteps == 0) {
            _liveRun.update { it.copy(status = WorkoutStatus.IDLE) }
            return
        }
        val autoSync = uiState.value.preferences.isStravaAutoSync
        val encodedRoute = run.routePoints.joinToString(";") { "${it.xFraction},${it.yFraction}" }
        val finalDistance = if (run.sessionDistanceKm > 0.01) run.sessionDistanceKm else 0.05
        val finalCalories = maxOf(run.sessionCalories, 5)
        val finalSteps = maxOf(run.sessionSteps, 60)

        viewModelScope.launch {
            repository.saveWorkoutAndDailyIncrement(
                title = "Sesi Lari Outdoor",
                distanceKm = finalDistance,
                durationSeconds = run.elapsedSeconds,
                caloriesBurned = finalCalories,
                avgBpm = run.avgBpm,
                maxBpm = run.maxBpm,
                stepsAdded = finalSteps,
                paceDisplay = run.paceDisplay,
                routeName = if (run.hasGpsFix) "Rute GPS Terlacak (${run.gpsPointCount} titik)" else "Lintasan Lari",
                routePointsEncoded = encodedRoute,
                syncedToStrava = autoSync
            )
            audioCoach.sfxFanfare()
            audioCoach.vibrateShort()
            _liveRun.update {
                it.copy(
                    status = WorkoutStatus.IDLE,
                    elapsedSeconds = 0L,
                    sessionSteps = 0,
                    sessionDistanceKm = 0.0,
                    sessionCalories = 0,
                    paceDisplay = "0'00\"",
                    currentPaceDisplay = "0'00\"",
                    currentSpeedKmh = 0.0,
                    elevationGainMeters = 0.0,
                    gpsPointCount = 0,
                    currentBpm = 80
                )
            }
            showToast(
                if (autoSync) "Aktivitas selesai & tersinkron ke Strava!"
                else "Aktivitas selesai & tersimpan di riwayat!"
            )
        }
    }

    // ==================== PENJAS EXERCISE & VIDEO DEMO ====================

    private var activeSessionQueue: List<Int> = emptyList()
    private var activeQueuePosition: Int = 0

    fun selectExercise(index: Int) {
        haltExercise()
        val exercise = PenjasCatalog.findExercise(index)
        _penjas.update {
            it.copy(
                selectedExerciseIndex = exercise.id,
                timeLeftSeconds = exercise.durationSeconds,
                totalTimeSeconds = exercise.durationSeconds,
                statusText = "Siap",
                demoCaption = DEFAULT_DEMO_CAPTION,
                highlightedStepIndex = -1,
                activeQueueLabel = ""
            )
        }
    }

    fun toggleVideoDemo() {
        val state = _penjas.value
        if (state.isDemoActive) {
            stopVideoDemo()
            audioCoach.stopSpeaking()
            return
        }
        haltExercise()
        val ex = state.selectedExercise
        demoQueue = listOf("${ex.name}. ${ex.description}") +
            ex.steps.mapIndexed { idx, step -> "Langkah ${idx + 1}. $step" } +
            listOf("Manfaat: ${ex.benefits} Target: ${ex.targetReps}.")

        _penjas.update {
            it.copy(
                isDemoActive = true,
                isDemoPaused = false,
                demoStepIndex = 0,
                demoTotalSteps = demoQueue.size,
                highlightedStepIndex = -1
            )
        }
        runDemoStep(0)
    }

    private fun runDemoStep(stepIndex: Int) {
        demoStepJob?.cancel()
        if (stepIndex >= demoQueue.size) {
            audioCoach.speak("Video selesai. Sekarang coba gerakannya!")
            stopVideoDemo(completed = true)
            return
        }
        val safeIdx = stepIndex.coerceAtLeast(0)
        val caption = demoQueue[safeIdx]
        _penjas.update {
            it.copy(
                demoStepIndex = safeIdx,
                demoCaption = caption,
                highlightedStepIndex = safeIdx - 1
            )
        }
        audioCoach.speak(caption)
        val durationMs = maxOf(4500L, caption.length * 80L)
        demoStepJob = viewModelScope.launch {
            delay(durationMs)
            runDemoStep(safeIdx + 1)
        }
    }

    fun goDemoStep(delta: Int) {
        if (!_penjas.value.isDemoActive) return
        val next = (_penjas.value.demoStepIndex + delta).coerceAtLeast(0)
        _penjas.update { it.copy(isDemoPaused = false) }
        runDemoStep(next)
    }

    fun toggleDemoPause() {
        val state = _penjas.value
        if (!state.isDemoActive) {
            toggleVideoDemo()
            return
        }
        if (!state.isDemoPaused) {
            demoStepJob?.cancel()
            audioCoach.stopSpeaking()
            _penjas.update { it.copy(isDemoPaused = true) }
        } else {
            _penjas.update { it.copy(isDemoPaused = false) }
            runDemoStep(state.demoStepIndex)
        }
    }

    private fun stopVideoDemo(completed: Boolean = false) {
        demoStepJob?.cancel()
        demoStepJob = null
        _penjas.update {
            it.copy(
                isDemoActive = false,
                isDemoPaused = false,
                demoStepIndex = if (completed) it.demoTotalSteps else 0,
                demoCaption = if (completed) "Video selesai. Sekarang tekan “Coba Gerakan”!" else DEFAULT_DEMO_CAPTION,
                highlightedStepIndex = -1
            )
        }
    }

    fun speakCurrentExercise() {
        val ex = _penjas.value.selectedExercise
        audioCoach.speak("${ex.name}. ${ex.description} ${ex.steps.joinToString(" ")} Manfaat: ${ex.benefits}")
    }

    fun toggleSingleExercise() {
        stopVideoDemo()
        if (_penjas.value.isExerciseRunning) {
            haltExercise()
            return
        }
        activeSessionQueue = emptyList()
        activeQueuePosition = 0
        startExerciseAt(_penjas.value.selectedExerciseIndex, isRest = false, autoSession = false)
    }

    fun startFullMorningSession() {
        haltExercise()
        activeSessionQueue = PenjasCatalog.exercises.map { it.id }
        activeQueuePosition = 0
        showToast("Memulai Sesi Pagi Lengkap (8 Gerakan)")
        startExerciseAt(activeSessionQueue.first(), isRest = false, autoSession = true, queueLabel = "Sesi Pagi 1/8")
    }

    fun startRecommendedWarmupSession() {
        haltExercise()
        val plan = uiState.value.warmupPlan
        val ids = plan.recommendations.map { it.exercise.id }
        if (ids.isEmpty()) return
        activeSessionQueue = ids
        activeQueuePosition = 0
        showToast("Memulai ${plan.planTitle} (${ids.size} Gerakan)")
        startExerciseAt(
            index = ids.first(),
            isRest = false,
            autoSession = true,
            queueLabel = "Paket Cerdas 1/${ids.size}"
        )
    }

    private fun startExerciseAt(
        index: Int,
        isRest: Boolean,
        autoSession: Boolean,
        queueLabel: String = ""
    ) {
        exerciseTimerJob?.cancel()
        exerciseBeatJob?.cancel()
        val ex = PenjasCatalog.findExercise(index)
        val duration = if (isRest) 5 else ex.durationSeconds

        _penjas.update {
            it.copy(
                selectedExerciseIndex = ex.id,
                isExerciseRunning = true,
                isAutoSession = autoSession,
                isRestPhase = isRest,
                timeLeftSeconds = duration,
                totalTimeSeconds = duration,
                statusText = if (isRest) "Istirahat → ${ex.name}" else "Ayo bergerak!",
                activeQueueLabel = queueLabel
            )
        }

        if (isRest) {
            audioCoach.speak("Istirahat sebentar. Berikutnya, ${ex.name}")
        } else {
            audioCoach.speak("Mulai! ${ex.name}")
            exerciseBeatJob = viewModelScope.launch {
                var beatCount = 0
                while (isActive) {
                    audioCoach.playBeat(isAccent = beatCount++ % 4 == 0)
                    delay(600L)
                }
            }
        }

        exerciseTimerJob = viewModelScope.launch {
            var remaining = duration
            while (remaining > 0 && isActive) {
                delay(1000L)
                remaining--
                _penjas.update { it.copy(timeLeftSeconds = remaining.coerceAtLeast(0)) }
                if (remaining in 1..3) {
                    audioCoach.playCountdownBeep()
                }
            }
            if (!isActive) return@launch
            exerciseBeatJob?.cancel()
            audioCoach.sfxGood()
            audioCoach.vibrateShort()

            if (!isRest) {
                repository.markPenjasExerciseCompleted(ex.id, ex.name)
                showToast("Gerakan “${ex.name}” selesai! (+65 langkah)")
            }

            if (autoSession && isRest) {
                val label = if (activeSessionQueue.isNotEmpty()) {
                    "Gerakan ${activeQueuePosition + 1}/${activeSessionQueue.size}"
                } else ""
                startExerciseAt(ex.id, isRest = false, autoSession = true, queueLabel = label)
            } else if (autoSession && activeQueuePosition < activeSessionQueue.lastIndex) {
                activeQueuePosition++
                val nextId = activeSessionQueue[activeQueuePosition]
                val label = "Berikutnya ${activeQueuePosition + 1}/${activeSessionQueue.size}"
                startExerciseAt(nextId, isRest = true, autoSession = true, queueLabel = label)
            } else {
                val finishedFull = autoSession
                activeSessionQueue = emptyList()
                activeQueuePosition = 0
                haltExercise(keepStatus = true)
                _penjas.update {
                    it.copy(
                        statusText = if (finishedFull) "Sesi Selesai!" else "Selesai!",
                        activeQueueLabel = ""
                    )
                }
                audioCoach.speak(
                    if (finishedFull) "Pemanasan selesai. Tubuhmu siap berlari!"
                    else "Bagus sekali!"
                )
                if (finishedFull) {
                    delay(400L)
                    audioCoach.sfxFanfare()
                }
            }
        }
    }

    fun haltExercise(keepStatus: Boolean = false) {
        stopVideoDemo()
        exerciseTimerJob?.cancel()
        exerciseBeatJob?.cancel()
        exerciseTimerJob = null
        exerciseBeatJob = null
        if (!keepStatus) {
            audioCoach.stopSpeaking()
            activeSessionQueue = emptyList()
            activeQueuePosition = 0
        }
        val ex = _penjas.value.selectedExercise
        _penjas.update {
            it.copy(
                isExerciseRunning = false,
                isAutoSession = false,
                isRestPhase = false,
                timeLeftSeconds = ex.durationSeconds,
                totalTimeSeconds = ex.durationSeconds,
                statusText = if (keepStatus) it.statusText else "Siap",
                activeQueueLabel = if (keepStatus) it.activeQueueLabel else ""
            )
        }
    }

    // ==================== PENJAS & WEEKLY HEALTH QUIZ ====================

    fun selectQuizMode(mode: QuizMode) {
        audioCoach.stopSpeaking()
        _quiz.value = QuizUiState(
            mode = mode,
            selectedWeekIndex = if (mode == QuizMode.WEEKLY) {
                _quiz.value.selectedWeekIndex
            } else {
                repository.currentCycleWeekIndex()
            }
        )
    }

    fun selectWeeklyTheme(weekIndex: Int) {
        audioCoach.stopSpeaking()
        _quiz.value = QuizUiState(
            mode = QuizMode.WEEKLY,
            selectedWeekIndex = weekIndex.coerceIn(1, WeeklyQuizCatalog.weeks.size)
        )
    }

    fun speakCurrentQuizQuestion() {
        val state = _quiz.value
        val q = state.questions.getOrNull(state.currentIndex) ?: return
        val letters = listOf("A", "B", "C", "D")
        val optionsSpoken = q.options.mapIndexed { idx, opt -> "Pilihan ${letters[idx]}. $opt" }.joinToString(". ")
        audioCoach.speak("${q.question}. $optionsSpoken")
    }

    fun answerQuizOption(optionIndex: Int) {
        val state = _quiz.value
        if (state.selectedOptionIndex != null || state.isFinished) return
        val q = state.questions.getOrNull(state.currentIndex) ?: return
        val isCorrect = optionIndex == q.correctIndex
        if (isCorrect) audioCoach.sfxGood() else audioCoach.sfxBad()
        _quiz.update { current ->
            if (isCorrect) {
                val nextStreak = current.currentStreak + 1
                val streakBonus = (nextStreak - 1).coerceAtLeast(0) * 25
                val earned = 100 + streakBonus
                current.copy(
                    selectedOptionIndex = optionIndex,
                    score = current.score + 1,
                    points = current.points + earned,
                    currentStreak = nextStreak,
                    bestStreak = maxOf(current.bestStreak, nextStreak),
                    lastPointsEarned = earned
                )
            } else {
                current.copy(
                    selectedOptionIndex = optionIndex,
                    currentStreak = 0,
                    lastPointsEarned = 0
                )
            }
        }
    }

    fun nextQuizQuestion() {
        val state = _quiz.value
        val total = state.questions.size
        val nextIndex = state.currentIndex + 1
        if (nextIndex >= total) {
            val finalScore = state.score
            val finalPoints = state.points
            val bestStreak = state.bestStreak
            val pct = if (total > 0) (finalScore * 100) / total else 0
            val msg = when {
                pct >= 90 -> "Luar biasa! Kamu meraih predikat Master Kebugaran."
                pct >= 75 -> "Hebat! Pengetahuan kesehatan dan olahragamu sangat baik."
                pct >= 50 -> "Bagus! Terus pelajari edukasi kebugaran tiap minggu."
                else -> "Semangat belajar! Ulangi kuis untuk meningkatkan skormu."
            }
            val activeUser = uiState.value.preferences.athleteName
            viewModelScope.launch {
                repository.saveQuizResult(
                    quizTitle = state.activeTitle,
                    quizMode = state.mode.storageKey,
                    athleteName = activeUser,
                    score = finalScore,
                    totalQuestions = total,
                    points = finalPoints,
                    bestStreak = bestStreak
                )
            }
            audioCoach.sfxFanfare()
            audioCoach.speak("Skor kamu $finalScore dari $total dengan $finalPoints poin. $msg")
            _quiz.update { it.copy(isFinished = true) }
        } else {
            _quiz.update {
                it.copy(
                    currentIndex = nextIndex,
                    selectedOptionIndex = null,
                    lastPointsEarned = 0
                )
            }
        }
    }

    fun restartQuiz() {
        val current = _quiz.value
        _quiz.value = QuizUiState(
            mode = current.mode,
            selectedWeekIndex = current.selectedWeekIndex
        )
    }

    fun resetPenjasProgress() {
        viewModelScope.launch {
            repository.resetPenjasProgress()
            showToast("Progres Penjas & skor kuis telah direset")
        }
    }

    override fun onCleared() {
        super.onCleared()
        workoutTimerJob?.cancel()
        exerciseTimerJob?.cancel()
        exerciseBeatJob?.cancel()
        demoStepJob?.cancel()
        sensorManager.stopTracking()
        RunningTrackerService.stop(getApplication())
        audioCoach.release()
    }
}
