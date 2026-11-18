package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(
    entities = [
        WorkoutActivityEntity::class,
        PenjasExerciseLogEntity::class,
        QuizResultEntity::class,
        DailyMetricEntity::class,
        AchievementUnlockEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PulseRunDatabase : RoomDatabase() {
    abstract fun pulseRunDao(): PulseRunDao

    companion object {
        @Volatile
        private var INSTANCE: PulseRunDatabase? = null

        fun getInstance(context: Context): PulseRunDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PulseRunDatabase::class.java,
                    "pulserun_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(SeedDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedDatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = database.pulseRunDao()
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val dayFmt = SimpleDateFormat("EEE", Locale("id", "ID"))

                    val defaultRoute = "0.10,0.73;0.23,0.19;0.43,0.50;0.73,0.31;0.90,0.65"
                    val now = System.currentTimeMillis()
                    dao.insertWorkout(
                        WorkoutActivityEntity(
                            title = "Lari Pagi Santai",
                            distanceKm = 5.12,
                            durationSeconds = 1660L,
                            calories = 410,
                            avgBpm = 138,
                            maxBpm = 156,
                            steps = 6820,
                            paceDisplay = "5'24\"",
                            routeName = "Summarecon Loop",
                            routePointsEncoded = defaultRoute,
                            syncedToStrava = true,
                            timestampMillis = now - 86_400_000L
                        )
                    )

                    val weeklySampleSteps = listOf(7200, 8500, 6100, 9800, 7400, 9470, 6420)
                    val metrics = mutableListOf<DailyMetricEntity>()
                    for (offset in 6 downTo 0) {
                        val cal = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -offset)
                        }
                        val idx = 6 - offset
                        val st = weeklySampleSteps[idx]
                        val label = dayFmt.format(cal.time).replaceFirstChar { it.uppercase() }.take(3)
                        metrics.add(
                            DailyMetricEntity(
                                dateKey = sdf.format(cal.time),
                                dayLabel = label,
                                steps = st,
                                calories = if (offset == 0) 432 else (st * 0.062).toInt(),
                                distanceKm = st * 0.00075,
                                timestampMillis = cal.timeInMillis
                            )
                        )
                    }
                    dao.insertDailyMetrics(metrics)
                }
            }
        }
    }
}
