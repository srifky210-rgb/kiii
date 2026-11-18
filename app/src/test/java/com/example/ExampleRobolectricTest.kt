package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PulseRun", appName)
  }

  @Test
  fun `penjas catalog has 8 exercises and 10 quiz questions`() {
    assertEquals(8, com.example.data.model.PenjasCatalog.exercises.size)
    assertEquals(10, com.example.data.model.PenjasCatalog.quizQuestions.size)
  }

  @Test
  fun `weekly quiz catalog has 4 themed weeks with valid questions`() {
    val weeks = com.example.data.model.WeeklyQuizCatalog.weeks
    assertEquals(4, weeks.size)
    weeks.forEach { week ->
      assertEquals(6, week.questions.size)
      week.questions.forEach { q ->
        assert(q.correctIndex in q.options.indices)
      }
    }
  }

  @Test
  fun `reminder scheduler creates channel and caches athlete schedule`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.notifications.ReminderScheduler.ensureChannel(context)
    val notifManager = context.getSystemService(android.app.NotificationManager::class.java)
    val channel = notifManager.getNotificationChannel(com.example.notifications.ReminderScheduler.CHANNEL_ID)
    assert(channel != null)

    com.example.notifications.ReminderScheduler.syncSchedules(
      context = context,
      athleteName = "Budi Santoso",
      runEnabled = true,
      runHour = 6,
      runMinute = 30,
      quizEnabled = true,
      quizDayOfWeek = java.util.Calendar.SUNDAY,
      quizHour = 9,
      quizMinute = 0
    )
    assertEquals("Budi Santoso", com.example.notifications.ReminderScheduler.getCachedAthleteName(context))
    com.example.notifications.ReminderScheduler.rescheduleFromCache(context)
  }

  @Test
  fun `app theme mode maps storage keys cleanly`() {
    assertEquals(com.example.data.local.AppThemeMode.SYSTEM, com.example.data.local.AppThemeMode.fromKey(null))
    assertEquals(com.example.data.local.AppThemeMode.LIGHT, com.example.data.local.AppThemeMode.fromKey("LIGHT"))
    assertEquals(com.example.data.local.AppThemeMode.DARK, com.example.data.local.AppThemeMode.fromKey("DARK"))
    assertEquals(com.example.data.local.AppThemeMode.SYSTEM, com.example.data.local.AppThemeMode.fromKey("SYSTEM"))
  }

  @Test
  fun `achievement catalog evaluates running streaks distance milestones and quiz scores`() {
    val dayMs = 86_400_000L
    val now = 1_795_000_000_000L
    val workouts = listOf(
      com.example.data.local.WorkoutActivityEntity(
        id = 1L,
        title = "Lari Hari 1",
        distanceKm = 3.5,
        durationSeconds = 1200L,
        calories = 220,
        avgBpm = 142,
        maxBpm = 160,
        steps = 4500,
        paceDisplay = "5'42\"",
        routeName = "Rute A",
        routePointsEncoded = "",
        syncedToStrava = true,
        timestampMillis = now
      ),
      com.example.data.local.WorkoutActivityEntity(
        id = 2L,
        title = "Lari Hari 2",
        distanceKm = 4.0,
        durationSeconds = 1350L,
        calories = 260,
        avgBpm = 145,
        maxBpm = 164,
        steps = 5200,
        paceDisplay = "5'37\"",
        routeName = "Rute B",
        routePointsEncoded = "",
        syncedToStrava = true,
        timestampMillis = now - dayMs
      ),
      com.example.data.local.WorkoutActivityEntity(
        id = 3L,
        title = "Lari Hari 3",
        distanceKm = 3.0,
        durationSeconds = 1050L,
        calories = 195,
        avgBpm = 138,
        maxBpm = 155,
        steps = 3900,
        paceDisplay = "5'50\"",
        routeName = "Rute C",
        routePointsEncoded = "",
        syncedToStrava = false,
        timestampMillis = now - 2 * dayMs
      )
    )
    val quizResults = listOf(
      com.example.data.local.QuizResultEntity(
        id = 1L,
        dateKey = "2026-11-18",
        weekKey = "2026-W47",
        quizTitle = "Minggu 1",
        quizMode = "WEEKLY",
        athleteName = "Rifky",
        score = 6,
        totalQuestions = 6,
        points = 1100,
        bestStreak = 6,
        timestampMillis = now
      )
    )

    val summary = com.example.data.model.AchievementCatalog.evaluate(
      workouts = workouts,
      penjasLogs = emptyList(),
      quizResults = quizResults,
      persistedUnlocks = emptyList(),
      referenceTimeMillis = now
    )

    assertEquals(3, summary.runningStreakDays)
    assertEquals(10.5, summary.totalRunDistanceKm, 0.01)
    assertEquals(100, summary.bestQuizPercentage)

    val unlockedIds = summary.items.filter { it.isUnlocked }.map { it.definition.id }.toSet()
    assert(unlockedIds.contains("first_activity_day"))
    assert(unlockedIds.contains("run_streak_3"))
    assert(unlockedIds.contains("distance_1k"))
    assert(unlockedIds.contains("distance_5k"))
    assert(unlockedIds.contains("distance_10k"))
    assert(unlockedIds.contains("single_run_3k"))
    assert(unlockedIds.contains("quiz_first"))
    assert(unlockedIds.contains("quiz_high_80"))
    assert(unlockedIds.contains("quiz_perfect_100"))
    assert(unlockedIds.contains("quiz_points_1000"))
    assert(!unlockedIds.contains("run_streak_7"))

    val newUnlocks = com.example.data.model.AchievementCatalog.findNewlyEarnedUnlocks(
      summary = summary,
      persistedUnlocks = listOf(
        com.example.data.local.AchievementUnlockEntity("first_activity_day", now, 3.0)
      ),
      unlockTimestampMillis = now
    )
    assert(newUnlocks.none { it.first.id == "first_activity_day" })
    assert(newUnlocks.any { it.first.id == "run_streak_3" })
  }

  @Test
  fun `weekly trend calculator computes 7 day distance and speed accurately`() {
    val now = 1_795_000_000_000L
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    val todayKey = sdf.format(java.util.Date(now))
    val yesterdayKey = sdf.format(java.util.Date(now - 86_400_000L))

    val metrics = listOf(
      com.example.data.local.DailyMetricEntity(
        dateKey = yesterdayKey,
        dayLabel = "Sel",
        steps = 6000,
        calories = 320,
        distanceKm = 4.5,
        timestampMillis = now - 86_400_000L
      ),
      com.example.data.local.DailyMetricEntity(
        dateKey = todayKey,
        dayLabel = "Rab",
        steps = 8000,
        calories = 450,
        distanceKm = 6.0,
        timestampMillis = now
      )
    )
    val workouts = listOf(
      com.example.data.local.WorkoutActivityEntity(
        id = 1L,
        title = "Lari Cepat",
        distanceKm = 6.0,
        durationSeconds = 1800L, // 30 minutes => 12.0 km/h
        calories = 420,
        avgBpm = 152,
        maxBpm = 170,
        steps = 7500,
        paceDisplay = "5'00\"",
        routeName = "Park Loop",
        routePointsEncoded = "",
        syncedToStrava = true,
        timestampMillis = now
      )
    )

    val summary = com.example.data.model.WeeklyTrendCalculator.buildWeeklyTrend(
      dailyMetrics = metrics,
      workouts = workouts,
      todayDateKey = todayKey,
      referenceTimeMillis = now
    )

    assertEquals(7, summary.points.size)
    val todayPoint = summary.points.last()
    assertEquals(todayKey, todayPoint.dateKey)
    assertEquals(6.0, todayPoint.distanceKm, 0.01)
    assertEquals(12.0, todayPoint.speedKmh, 0.05)
    assertEquals("5'00\"", todayPoint.paceDisplay)
    assertEquals(false, todayPoint.isEstimatedSpeed)
    assertEquals(10.5, summary.totalDistanceKm, 0.01)
    assertEquals(12.0, summary.peakSpeedKmh, 0.05)
  }

  @Test
  fun `open meteo weather mapper parses forecast and computes running advice`() {
    val response = com.example.data.remote.OpenMeteoForecastResponse(
      latitude = -6.21,
      longitude = 106.85,
      timezone = "Asia/Jakarta",
      current = com.example.data.remote.CurrentWeatherDto(
        time = "2026-11-18T07:00",
        temperature2m = 25.4,
        apparentTemperature = 27.0,
        relativeHumidity2m = 74,
        precipitation = 0.0,
        weatherCode = 1,
        windSpeed10m = 9.5
      ),
      hourly = com.example.data.remote.HourlyWeatherDto(
        time = listOf(
          "2026-11-18T06:00",
          "2026-11-18T07:00",
          "2026-11-18T08:00",
          "2026-11-18T09:00",
          "2026-11-18T10:00",
          "2026-11-18T11:00"
        ),
        temperature2m = listOf(24.0, 25.4, 27.1, 29.0, 30.8, 32.1),
        precipitationProbability = listOf(5, 10, 15, 25, 45, 70),
        precipitation = listOf(0.0, 0.0, 0.0, 0.1, 0.4, 1.8),
        weatherCode = listOf(0, 1, 2, 2, 51, 61)
      )
    )

    val state = com.example.data.remote.WmoWeatherMapper.mapResponseToState(
      response = response,
      locationLabel = "Menteng, Jakarta",
      isUsingFallbackLocation = false
    )

    assertEquals(true, state.hasLoadedData)
    assertEquals("Cerah Berawan", state.conditionLabel)
    assertEquals(10, state.precipProbabilityPct)
    assertEquals(com.example.data.remote.RunWeatherSeverity.IDEAL, state.runAdvice.severity)
    assertEquals(5, state.hourlySlots.size)
    assertEquals("07:00", state.hourlySlots.first().hourLabel)
    assertEquals("11:00", state.hourlySlots.last().hourLabel)
    assertEquals(70, state.hourlySlots.last().precipProbabilityPct)

    val rainyAdvice = com.example.data.remote.WmoWeatherMapper.evaluateRunningAdvice(
      temperatureC = 25.0,
      precipitationMm = 1.6,
      precipProbabilityPct = 80,
      weatherCode = 63
    )
    assertEquals(com.example.data.remote.RunWeatherSeverity.WARNING, rainyAdvice.severity)
  }

  @Test
  fun `warmup recommender adapts stretches to weather and recent running history`() {
    val now = 1_795_000_000_000L
    val longFastWorkouts = listOf(
      com.example.data.local.WorkoutActivityEntity(
        id = 1L,
        title = "Lari Cepat 6K",
        distanceKm = 6.2,
        durationSeconds = 1800L, // 12.4 km/h
        calories = 460,
        avgBpm = 154,
        maxBpm = 172,
        steps = 7800,
        paceDisplay = "4'50\"",
        routeName = "Stadion",
        routePointsEncoded = "",
        syncedToStrava = true,
        timestampMillis = now - 3600_000L
      )
    )

    val rainyWeather = com.example.data.remote.LocalWeatherState(
      hasLoadedData = true,
      temperatureC = 24.0,
      precipProbabilityPct = 75,
      precipitationMm = 1.4,
      weatherCode = 61,
      conditionEmoji = "🌧️"
    )
    val rainyPlan = com.example.data.model.WarmupRecommender.recommend(
      weather = rainyWeather,
      workouts = longFastWorkouts,
      nowMillis = now
    )
    assertEquals(com.example.data.model.WarmupContextType.RAIN_INDOOR, rainyPlan.contextType)
    assertEquals(4, rainyPlan.recommendations.size)
    val rainyIds = rainyPlan.recommendations.map { it.exercise.id }.toSet()
    assert(rainyIds.contains(1)) // Putar Lengan (indoor)
    assert(rainyIds.contains(10)) // Lunge Buka Pinggul (long run recovery)

    val coolWeather = com.example.data.remote.LocalWeatherState(
      hasLoadedData = true,
      temperatureC = 18.0,
      precipProbabilityPct = 5,
      precipitationMm = 0.0,
      weatherCode = 1,
      conditionEmoji = "🌤️"
    )
    val coolPlan = com.example.data.model.WarmupRecommender.recommend(
      weather = coolWeather,
      workouts = longFastWorkouts,
      nowMillis = now
    )
    assertEquals(com.example.data.model.WarmupContextType.COOL_WEATHER, coolPlan.contextType)
    val coolIds = coolPlan.recommendations.map { it.exercise.id }.toSet()
    assert(coolIds.contains(4)) // Lari di Tempat (warm muscles)
    assert(coolIds.contains(9) || coolIds.contains(11)) // Calf or Hamstring stretch
  }
}
