package com.example.data.repository

import android.content.Context
import com.example.data.local.AchievementUnlockEntity
import com.example.data.local.AppThemeMode
import com.example.data.local.DailyMetricEntity
import com.example.data.local.PenjasExerciseLogEntity
import com.example.data.local.PulseRunDao
import com.example.data.local.PulseRunDatabase
import com.example.data.local.QuizResultEntity
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.local.WorkoutActivityEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PulseRunRepository(
    private val dao: PulseRunDao,
    private val prefsRepo: UserPreferencesRepository
) {
    constructor(context: Context) : this(
        dao = PulseRunDatabase.getInstance(context).pulseRunDao(),
        prefsRepo = UserPreferencesRepository(context.applicationContext)
    )

    val workoutsFlow: Flow<List<WorkoutActivityEntity>> = dao.observeWorkouts()
    val penjasLogsFlow: Flow<List<PenjasExerciseLogEntity>> = dao.observePenjasLogs()
    val quizResultsFlow: Flow<List<QuizResultEntity>> = dao.observeQuizResults()
    val dailyMetricsFlow: Flow<List<DailyMetricEntity>> = dao.observeDailyMetrics()
    val achievementUnlocksFlow: Flow<List<AchievementUnlockEntity>> = dao.observeAchievementUnlocks()
    val userPreferencesFlow: Flow<UserPreferences> = prefsRepo.userPreferences

    fun todayDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun todayDayLabel(): String {
        val raw = SimpleDateFormat("EEE", Locale("id", "ID")).format(Date())
        return raw.replaceFirstChar { it.uppercase() }.take(3)
    }

    fun currentWeekKey(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        return String.format(Locale.US, "%d-W%02d", year, week)
    }

    fun currentCycleWeekIndex(): Int {
        val week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
        return ((week - 1) % 4) + 1
    }

    suspend fun loginAccount(accountName: String, accountEmail: String) =
        prefsRepo.loginAccount(accountName, accountEmail)

    suspend fun logoutAccount() = prefsRepo.logoutAccount()

    suspend fun setThemeMode(mode: AppThemeMode) = prefsRepo.setThemeMode(mode)
    suspend fun setDynamicColor(enabled: Boolean) = prefsRepo.setDynamicColor(enabled)
    suspend fun toggleSoundEnabled(current: Boolean) = prefsRepo.toggleSoundEnabled(current)
    suspend fun setStravaAutoSync(enabled: Boolean) = prefsRepo.setStravaAutoSync(enabled)
    suspend fun updateProfile(name: String, stepGoal: Int, calorieGoal: Int) =
        prefsRepo.updateProfile(name, stepGoal, calorieGoal)

    suspend fun updateRunReminder(enabled: Boolean, hour: Int, minute: Int) =
        prefsRepo.updateRunReminder(enabled, hour, minute)

    suspend fun updateQuizReminder(enabled: Boolean, dayOfWeek: Int, hour: Int, minute: Int) =
        prefsRepo.updateQuizReminder(enabled, dayOfWeek, hour, minute)

    suspend fun saveWorkoutAndDailyIncrement(
        title: String,
        distanceKm: Double,
        durationSeconds: Long,
        caloriesBurned: Int,
        avgBpm: Int,
        maxBpm: Int,
        stepsAdded: Int,
        paceDisplay: String,
        routeName: String,
        routePointsEncoded: String,
        syncedToStrava: Boolean
    ) {
        val now = System.currentTimeMillis()
        dao.insertWorkout(
            WorkoutActivityEntity(
                title = title,
                distanceKm = distanceKm,
                durationSeconds = durationSeconds,
                calories = caloriesBurned,
                avgBpm = avgBpm,
                maxBpm = maxBpm,
                steps = stepsAdded,
                paceDisplay = paceDisplay,
                routeName = routeName,
                routePointsEncoded = routePointsEncoded,
                syncedToStrava = syncedToStrava,
                timestampMillis = now
            )
        )
        addDailyMetrics(stepsAdded = stepsAdded, caloriesAdded = caloriesBurned, distanceAddedKm = distanceKm)
    }

    suspend fun toggleWorkoutSync(workoutId: Long, synced: Boolean) {
        dao.updateWorkoutSyncStatus(workoutId, synced)
    }

    suspend fun markPenjasExerciseCompleted(exerciseId: Int, exerciseName: String) {
        val dateKey = todayDateKey()
        val existing = dao.getPenjasLog(exerciseName, dateKey)
        val newCount = (existing?.completionCount ?: 0) + 1
        dao.upsertPenjasLog(
            PenjasExerciseLogEntity(
                exerciseName = exerciseName,
                dateKey = dateKey,
                exerciseId = exerciseId,
                completionCount = newCount,
                lastCompletedMillis = System.currentTimeMillis()
            )
        )
        addDailyMetrics(stepsAdded = 65, caloriesAdded = 8, distanceAddedKm = 65 * 0.00075)
    }

    suspend fun addDailyMetrics(stepsAdded: Int, caloriesAdded: Int, distanceAddedKm: Double) {
        val dateKey = todayDateKey()
        val current = dao.getDailyMetric(dateKey)
        val updated = if (current != null) {
            current.copy(
                steps = current.steps + stepsAdded,
                calories = current.calories + caloriesAdded,
                distanceKm = current.distanceKm + distanceAddedKm,
                timestampMillis = System.currentTimeMillis()
            )
        } else {
            DailyMetricEntity(
                dateKey = dateKey,
                dayLabel = todayDayLabel(),
                steps = stepsAdded,
                calories = caloriesAdded,
                distanceKm = distanceAddedKm,
                timestampMillis = System.currentTimeMillis()
            )
        }
        dao.upsertDailyMetric(updated)
    }

    suspend fun saveQuizResult(
        quizTitle: String,
        quizMode: String,
        athleteName: String,
        score: Int,
        totalQuestions: Int,
        points: Int,
        bestStreak: Int
    ) {
        dao.insertQuizResult(
            QuizResultEntity(
                dateKey = todayDateKey(),
                weekKey = currentWeekKey(),
                quizTitle = quizTitle,
                quizMode = quizMode,
                athleteName = athleteName.ifBlank { "Atlet" },
                score = score,
                totalQuestions = totalQuestions,
                points = points,
                bestStreak = bestStreak,
                timestampMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun recordAchievementUnlocks(unlocks: List<AchievementUnlockEntity>) {
        if (unlocks.isNotEmpty()) {
            dao.insertAchievementUnlocks(unlocks)
        }
    }

    suspend fun resetPenjasProgress() {
        dao.clearPenjasLogs()
        dao.clearQuizResults()
    }

    fun calculateStreakDays(logs: List<PenjasExerciseLogEntity>): Int {
        val activeDates = logs.map { it.dateKey }.toSet()
        if (activeDates.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        if (!activeDates.contains(sdf.format(cal.time))) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        var streak = 0
        while (activeDates.contains(sdf.format(cal.time))) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }
}
