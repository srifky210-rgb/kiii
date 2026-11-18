package com.example.data.model

import com.example.data.local.AchievementUnlockEntity
import com.example.data.local.PenjasExerciseLogEntity
import com.example.data.local.QuizResultEntity
import com.example.data.local.WorkoutActivityEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AchievementCategory(val label: String) {
    ALL("Semua"),
    STREAK("Streak"),
    DISTANCE("Jarak"),
    QUIZ("Kuis"),
    MILESTONE("Sesi")
}

enum class BadgeTier(val label: String, val points: Int) {
    BRONZE("Perunggu", 50),
    SILVER("Perak", 120),
    GOLD("Emas", 250),
    PLATINUM("Platina", 500)
}

enum class AchievementMetricType {
    ACTIVE_STREAK_DAYS,
    RUNNING_STREAK_DAYS,
    TOTAL_DISTANCE_KM,
    SINGLE_RUN_DISTANCE_KM,
    QUIZ_COUNT,
    BEST_QUIZ_PERCENTAGE,
    TOTAL_QUIZ_POINTS,
    UNIQUE_PENJAS_EXERCISES
}

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val tier: BadgeTier,
    val metricType: AchievementMetricType,
    val targetValue: Double,
    val unitLabel: String,
    val emoji: String,
    val actionHint: String
)

data class AchievementProgress(
    val definition: AchievementDefinition,
    val currentValue: Double,
    val isUnlocked: Boolean,
    val unlockedAtMillis: Long?,
    val progressFraction: Float,
    val progressText: String
)

data class AchievementSummary(
    val items: List<AchievementProgress> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
    val totalBadgePoints: Int = 0,
    val runningStreakDays: Int = 0,
    val activeStreakDays: Int = 0,
    val totalRunDistanceKm: Double = 0.0,
    val bestQuizPercentage: Int = 0,
    val totalQuizPoints: Int = 0,
    val athleteRankTitle: String = "Pelari Pemula",
    val nextTarget: AchievementProgress? = null
)

object AchievementCatalog {
    val definitions: List<AchievementDefinition> = listOf(
        // Streak Achievements
        AchievementDefinition(
            id = "first_activity_day",
            title = "Langkah Pembuka",
            description = "Catat hari aktif pertamamu lewat lari GPS atau pemanasan Penjas.",
            category = AchievementCategory.STREAK,
            tier = BadgeTier.BRONZE,
            metricType = AchievementMetricType.ACTIVE_STREAK_DAYS,
            targetValue = 1.0,
            unitLabel = "hari",
            emoji = "👟",
            actionHint = "Mulai sesi lari atau selesaikan 1 gerakan Penjas hari ini"
        ),
        AchievementDefinition(
            id = "run_streak_3",
            title = "Konsisten 3 Hari",
            description = "Lari rutin selama 3 hari berturut-turut tanpa putus.",
            category = AchievementCategory.STREAK,
            tier = BadgeTier.SILVER,
            metricType = AchievementMetricType.RUNNING_STREAK_DAYS,
            targetValue = 3.0,
            unitLabel = "hari",
            emoji = "🔥",
            actionHint = "Simpan minimal 1 aktivitas lari setiap hari selama 3 hari"
        ),
        AchievementDefinition(
            id = "run_streak_7",
            title = "Pejuang Sepekan",
            description = "Pertahankan streak lari rutin selama 7 hari penuh.",
            category = AchievementCategory.STREAK,
            tier = BadgeTier.GOLD,
            metricType = AchievementMetricType.RUNNING_STREAK_DAYS,
            targetValue = 7.0,
            unitLabel = "hari",
            emoji = "⚡",
            actionHint = "Aktifkan pengingat lari harian agar streak 7 hari tidak terputus"
        ),
        AchievementDefinition(
            id = "active_streak_14",
            title = "Disiplin Dua Pekan",
            description = "Bangun kebiasaan aktif selama 14 hari berturut-turut.",
            category = AchievementCategory.STREAK,
            tier = BadgeTier.PLATINUM,
            metricType = AchievementMetricType.ACTIVE_STREAK_DAYS,
            targetValue = 14.0,
            unitLabel = "hari",
            emoji = "👑",
            actionHint = "Gabungkan sesi lari atau Penjas setiap hari selama 2 minggu"
        ),

        // Distance Milestone Achievements
        AchievementDefinition(
            id = "distance_1k",
            title = "Kilometer Pertama",
            description = "Capai total jarak lari akumulatif sejauh 1 kilometer.",
            category = AchievementCategory.DISTANCE,
            tier = BadgeTier.BRONZE,
            metricType = AchievementMetricType.TOTAL_DISTANCE_KM,
            targetValue = 1.0,
            unitLabel = "km",
            emoji = "📍",
            actionHint = "Gunakan pelacak GPS di tab Hari Ini untuk mencatat lari 1 km"
        ),
        AchievementDefinition(
            id = "distance_5k",
            title = "Pelari 5K",
            description = "Kumpulkan total akumulasi jarak tempuh lari sejauh 5 km.",
            category = AchievementCategory.DISTANCE,
            tier = BadgeTier.SILVER,
            metricType = AchievementMetricType.TOTAL_DISTANCE_KM,
            targetValue = 5.0,
            unitLabel = "km",
            emoji = "🏃",
            actionHint = "Tambahkan sesi lari hingga total jarak mencapai 5.0 km"
        ),
        AchievementDefinition(
            id = "distance_10k",
            title = "Penakluk 10K",
            description = "Capai total jarak lari akumulatif sejauh 10 kilometer.",
            category = AchievementCategory.DISTANCE,
            tier = BadgeTier.GOLD,
            metricType = AchievementMetricType.TOTAL_DISTANCE_KM,
            targetValue = 10.0,
            unitLabel = "km",
            emoji = "🏅",
            actionHint = "Konsisten menambah jarak lari mingguan hingga 10.0 km"
        ),
        AchievementDefinition(
            id = "distance_21k",
            title = "Half Marathoner",
            description = "Capai total jarak tempuh setengah maraton (21.1 km).",
            category = AchievementCategory.DISTANCE,
            tier = BadgeTier.GOLD,
            metricType = AchievementMetricType.TOTAL_DISTANCE_KM,
            targetValue = 21.1,
            unitLabel = "km",
            emoji = "🌟",
            actionHint = "Terus kumpulkan kilometer lari hingga mencapai 21.1 km"
        ),
        AchievementDefinition(
            id = "distance_42k",
            title = "Legenda Marathon",
            description = "Tembus akumulasi jarak lari penuh sejauh 42.2 kilometer.",
            category = AchievementCategory.DISTANCE,
            tier = BadgeTier.PLATINUM,
            metricType = AchievementMetricType.TOTAL_DISTANCE_KM,
            targetValue = 42.2,
            unitLabel = "km",
            emoji = "🏆",
            actionHint = "Selesaikan rangkaian sesi lari jarak jauh hingga 42.2 km"
        ),

        // Quiz & Health Education Achievements
        AchievementDefinition(
            id = "quiz_first",
            title = "Pelajar Kebugaran",
            description = "Selesaikan sesi kuis kesehatan atau Penjas pertamamu.",
            category = AchievementCategory.QUIZ,
            tier = BadgeTier.BRONZE,
            metricType = AchievementMetricType.QUIZ_COUNT,
            targetValue = 1.0,
            unitLabel = "kuis",
            emoji = "📘",
            actionHint = "Buka tab Penjas → Kuis Mingguan dan selesaikan 1 sesi"
        ),
        AchievementDefinition(
            id = "quiz_high_80",
            title = "Cerdas Kebugaran",
            description = "Raih skor akurasi minimal 80% dalam satu sesi kuis.",
            category = AchievementCategory.QUIZ,
            tier = BadgeTier.SILVER,
            metricType = AchievementMetricType.BEST_QUIZ_PERCENTAGE,
            targetValue = 80.0,
            unitLabel = "%",
            emoji = "🧠",
            actionHint = "Jawab benar minimal 5 dari 6 soal pada Kuis Mingguan"
        ),
        AchievementDefinition(
            id = "quiz_perfect_100",
            title = "Master Skor Sempurna",
            description = "Jawab 100% pertanyaan dengan benar dalam satu sesi kuis!",
            category = AchievementCategory.QUIZ,
            tier = BadgeTier.GOLD,
            metricType = AchievementMetricType.BEST_QUIZ_PERCENTAGE,
            targetValue = 100.0,
            unitLabel = "%",
            emoji = "🎯",
            actionHint = "Selesaikan seluruh soal kuis tanpa satu pun jawaban salah"
        ),
        AchievementDefinition(
            id = "quiz_points_1000",
            title = "Kolektor Poin Kuis",
            description = "Kumpulkan total 1.000 poin dari berbagai tema kuis mingguan.",
            category = AchievementCategory.QUIZ,
            tier = BadgeTier.PLATINUM,
            metricType = AchievementMetricType.TOTAL_QUIZ_POINTS,
            targetValue = 1000.0,
            unitLabel = "poin",
            emoji = "💎",
            actionHint = "Bangun streak jawaban benar di Kuis Mingguan untuk bonus poin"
        ),

        // Single Run & Warm-up Milestones
        AchievementDefinition(
            id = "single_run_3k",
            title = "Endurance 3K",
            description = "Tempuh jarak minimal 3.0 km dalam satu sesi lari tunggal.",
            category = AchievementCategory.MILESTONE,
            tier = BadgeTier.SILVER,
            metricType = AchievementMetricType.SINGLE_RUN_DISTANCE_KM,
            targetValue = 3.0,
            unitLabel = "km",
            emoji = "🛣️",
            actionHint = "Lari sejauh 3.0 km dalam satu sesi sebelum menekan Selesai"
        ),
        AchievementDefinition(
            id = "penjas_all_8",
            title = "Atlet Pemanasan",
            description = "Selesaikan seluruh 8 jenis gerakan pemanasan Penjas.",
            category = AchievementCategory.MILESTONE,
            tier = BadgeTier.GOLD,
            metricType = AchievementMetricType.UNIQUE_PENJAS_EXERCISES,
            targetValue = 8.0,
            unitLabel = "gerakan",
            emoji = "🤸",
            actionHint = "Jalankan Sesi Pagi Lengkap di tab Penjas hingga semua selesai"
        )
    )

    fun calculateRunningStreakDays(
        workouts: List<WorkoutActivityEntity>,
        referenceTimeMillis: Long = System.currentTimeMillis()
    ): Int {
        if (workouts.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val activeDates = workouts.map { sdf.format(Date(it.timestampMillis)) }.toSet()
        return countConsecutiveDays(activeDates, referenceTimeMillis)
    }

    fun calculateActiveStreakDays(
        workouts: List<WorkoutActivityEntity>,
        penjasLogs: List<PenjasExerciseLogEntity>,
        referenceTimeMillis: Long = System.currentTimeMillis()
    ): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val workoutDates = workouts.map { sdf.format(Date(it.timestampMillis)) }
        val penjasDates = penjasLogs.map { it.dateKey }
        val activeDates = (workoutDates + penjasDates).toSet()
        return countConsecutiveDays(activeDates, referenceTimeMillis)
    }

    private fun countConsecutiveDays(activeDates: Set<String>, referenceTimeMillis: Long): Int {
        if (activeDates.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance().apply { timeInMillis = referenceTimeMillis }
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

    fun evaluate(
        workouts: List<WorkoutActivityEntity>,
        penjasLogs: List<PenjasExerciseLogEntity>,
        quizResults: List<QuizResultEntity>,
        persistedUnlocks: List<AchievementUnlockEntity>,
        referenceTimeMillis: Long = System.currentTimeMillis()
    ): AchievementSummary {
        val unlockMap = persistedUnlocks.associateBy { it.badgeId }
        val runningStreak = calculateRunningStreakDays(workouts, referenceTimeMillis)
        val activeStreak = calculateActiveStreakDays(workouts, penjasLogs, referenceTimeMillis)
        val totalDistanceKm = workouts.sumOf { it.distanceKm }
        val bestSingleRunKm = workouts.maxOfOrNull { it.distanceKm } ?: 0.0
        val quizCount = quizResults.size.toDouble()
        val bestQuizPct = quizResults.maxOfOrNull { res ->
            if (res.totalQuestions > 0) ((res.score * 100.0) / res.totalQuestions) else 0.0
        } ?: 0.0
        val totalQuizPoints = quizResults.sumOf { it.points }
        val uniquePenjasCount = penjasLogs.map { it.exerciseName }.distinct().size.toDouble()

        val items = definitions.map { def ->
            val rawValue = when (def.metricType) {
                AchievementMetricType.ACTIVE_STREAK_DAYS -> activeStreak.toDouble()
                AchievementMetricType.RUNNING_STREAK_DAYS -> runningStreak.toDouble()
                AchievementMetricType.TOTAL_DISTANCE_KM -> totalDistanceKm
                AchievementMetricType.SINGLE_RUN_DISTANCE_KM -> bestSingleRunKm
                AchievementMetricType.QUIZ_COUNT -> quizCount
                AchievementMetricType.BEST_QUIZ_PERCENTAGE -> bestQuizPct
                AchievementMetricType.TOTAL_QUIZ_POINTS -> totalQuizPoints.toDouble()
                AchievementMetricType.UNIQUE_PENJAS_EXERCISES -> uniquePenjasCount
            }

            val persisted = unlockMap[def.id]
            val unlocked = persisted != null || rawValue >= def.targetValue
            val displayValue = if (unlocked) maxOf(rawValue, def.targetValue) else rawValue
            val fraction = if (unlocked) {
                1f
            } else {
                (rawValue / def.targetValue).coerceIn(0.0, 1.0).toFloat()
            }

            AchievementProgress(
                definition = def,
                currentValue = displayValue,
                isUnlocked = unlocked,
                unlockedAtMillis = persisted?.unlockedAtMillis,
                progressFraction = fraction,
                progressText = formatProgress(def, rawValue.coerceAtMost(def.targetValue), unlocked)
            )
        }

        val unlockedItems = items.filter { it.isUnlocked }
        val unlockedCount = unlockedItems.size
        val totalPoints = unlockedItems.sumOf { it.definition.tier.points }
        val rankTitle = when {
            unlockedCount >= 13 -> "Legenda PulseRun"
            unlockedCount >= 9 -> "Master Kebugaran"
            unlockedCount >= 5 -> "Juara Konsisten"
            unlockedCount >= 2 -> "Pelari Aktif"
            else -> "Pelari Pemula"
        }
        val nextTarget = items
            .filter { !it.isUnlocked }
            .maxByOrNull { it.progressFraction }

        return AchievementSummary(
            items = items,
            unlockedCount = unlockedCount,
            totalCount = items.size,
            totalBadgePoints = totalPoints,
            runningStreakDays = runningStreak,
            activeStreakDays = activeStreak,
            totalRunDistanceKm = totalDistanceKm,
            bestQuizPercentage = bestQuizPct.toInt(),
            totalQuizPoints = totalQuizPoints,
            athleteRankTitle = rankTitle,
            nextTarget = nextTarget
        )
    }

    fun findNewlyEarnedUnlocks(
        summary: AchievementSummary,
        persistedUnlocks: List<AchievementUnlockEntity>,
        unlockTimestampMillis: Long = System.currentTimeMillis()
    ): List<Pair<AchievementDefinition, AchievementUnlockEntity>> {
        val existingIds = persistedUnlocks.map { it.badgeId }.toSet()
        return summary.items
            .filter { it.isUnlocked && !existingIds.contains(it.definition.id) }
            .map { item ->
                item.definition to AchievementUnlockEntity(
                    badgeId = item.definition.id,
                    unlockedAtMillis = unlockTimestampMillis,
                    progressValue = item.currentValue
                )
            }
    }

    private fun formatProgress(
        def: AchievementDefinition,
        clampedValue: Double,
        unlocked: Boolean
    ): String {
        val shownValue = if (unlocked) def.targetValue else clampedValue
        return if (def.unitLabel == "km") {
            String.format(Locale.US, "%.1f / %.1f km", shownValue, def.targetValue)
        } else {
            "${shownValue.toInt()} / ${def.targetValue.toInt()} ${def.unitLabel}"
        }
    }
}
