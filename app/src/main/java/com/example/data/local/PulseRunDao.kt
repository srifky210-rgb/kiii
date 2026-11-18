package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PulseRunDao {
    @Query("SELECT * FROM workout_activities ORDER BY timestampMillis DESC")
    fun observeWorkouts(): Flow<List<WorkoutActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutActivityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(workouts: List<WorkoutActivityEntity>)

    @Query("UPDATE workout_activities SET syncedToStrava = :synced WHERE id = :id")
    suspend fun updateWorkoutSyncStatus(id: Long, synced: Boolean)

    @Query("SELECT * FROM penjas_exercise_logs")
    fun observePenjasLogs(): Flow<List<PenjasExerciseLogEntity>>

    @Query("SELECT * FROM penjas_exercise_logs WHERE exerciseName = :name AND dateKey = :dateKey LIMIT 1")
    suspend fun getPenjasLog(name: String, dateKey: String): PenjasExerciseLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPenjasLog(log: PenjasExerciseLogEntity)

    @Query("DELETE FROM penjas_exercise_logs")
    suspend fun clearPenjasLogs()

    @Query("SELECT * FROM quiz_results ORDER BY timestampMillis DESC")
    fun observeQuizResults(): Flow<List<QuizResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResultEntity)

    @Query("DELETE FROM quiz_results")
    suspend fun clearQuizResults()

    @Query("SELECT * FROM daily_metrics ORDER BY timestampMillis ASC")
    fun observeDailyMetrics(): Flow<List<DailyMetricEntity>>

    @Query("SELECT * FROM daily_metrics WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getDailyMetric(dateKey: String): DailyMetricEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyMetric(metric: DailyMetricEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyMetrics(metrics: List<DailyMetricEntity>)

    @Query("SELECT * FROM achievement_unlocks ORDER BY unlockedAtMillis DESC")
    fun observeAchievementUnlocks(): Flow<List<AchievementUnlockEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievementUnlocks(unlocks: List<AchievementUnlockEntity>)
}
