package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_activities")
data class WorkoutActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val distanceKm: Double,
    val durationSeconds: Long,
    val calories: Int,
    val avgBpm: Int,
    val maxBpm: Int,
    val steps: Int,
    val paceDisplay: String,
    val routeName: String,
    val routePointsEncoded: String,
    val syncedToStrava: Boolean,
    val timestampMillis: Long
)

@Entity(tableName = "penjas_exercise_logs", primaryKeys = ["exerciseName", "dateKey"])
data class PenjasExerciseLogEntity(
    val exerciseName: String,
    val dateKey: String,
    val exerciseId: Int,
    val completionCount: Int,
    val lastCompletedMillis: Long
)

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String,
    val weekKey: String,
    val quizTitle: String,
    val quizMode: String,
    val athleteName: String,
    val score: Int,
    val totalQuestions: Int,
    val points: Int,
    val bestStreak: Int,
    val timestampMillis: Long
)

@Entity(tableName = "daily_metrics")
data class DailyMetricEntity(
    @PrimaryKey val dateKey: String,
    val dayLabel: String,
    val steps: Int,
    val calories: Int,
    val distanceKm: Double,
    val timestampMillis: Long
)

@Entity(tableName = "achievement_unlocks")
data class AchievementUnlockEntity(
    @PrimaryKey val badgeId: String,
    val unlockedAtMillis: Long,
    val progressValue: Double
)
