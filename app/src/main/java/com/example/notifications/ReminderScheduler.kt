package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.WeeklyQuizCatalog
import java.util.Calendar

object ReminderScheduler {
    const val CHANNEL_ID = "pulserun_reminders"
    const val ACTION_RUN_REMINDER = "com.example.action.RUN_REMINDER"
    const val ACTION_QUIZ_REMINDER = "com.example.action.QUIZ_REMINDER"
    const val EXTRA_TARGET_TAB = "extra_target_tab"
    const val TAB_DASHBOARD = "dashboard"
    const val TAB_WEEKLY_QUIZ = "weekly_quiz"

    private const val PREFS_NAME = "pulserun_reminder_cache"
    private const val KEY_ATHLETE_NAME = "athlete_name"
    private const val KEY_RUN_ENABLED = "run_enabled"
    private const val KEY_RUN_HOUR = "run_hour"
    private const val KEY_RUN_MINUTE = "run_minute"
    private const val KEY_QUIZ_ENABLED = "quiz_enabled"
    private const val KEY_QUIZ_DAY = "quiz_day"
    private const val KEY_QUIZ_HOUR = "quiz_hour"
    private const val KEY_QUIZ_MINUTE = "quiz_minute"

    private const val REQ_RUN_ALARM = 5101
    private const val REQ_QUIZ_ALARM = 5102
    private const val NOTIF_ID_RUN = 6101
    private const val NOTIF_ID_QUIZ = 6102

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Pengingat Lari & Kuis Kesehatan",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifikasi pengingat rutin untuk lari pagi/sore dan kuis kesehatan mingguan."
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun syncSchedules(
        context: Context,
        athleteName: String,
        runEnabled: Boolean,
        runHour: Int,
        runMinute: Int,
        quizEnabled: Boolean,
        quizDayOfWeek: Int,
        quizHour: Int,
        quizMinute: Int
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ATHLETE_NAME, athleteName)
            .putBoolean(KEY_RUN_ENABLED, runEnabled)
            .putInt(KEY_RUN_HOUR, runHour)
            .putInt(KEY_RUN_MINUTE, runMinute)
            .putBoolean(KEY_QUIZ_ENABLED, quizEnabled)
            .putInt(KEY_QUIZ_DAY, quizDayOfWeek)
            .putInt(KEY_QUIZ_HOUR, quizHour)
            .putInt(KEY_QUIZ_MINUTE, quizMinute)
            .apply()

        scheduleRunReminder(context, runEnabled, runHour, runMinute)
        scheduleWeeklyQuizReminder(context, quizEnabled, quizDayOfWeek, quizHour, quizMinute)
    }

    fun rescheduleFromCache(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val runEnabled = prefs.getBoolean(KEY_RUN_ENABLED, true)
        val runHour = prefs.getInt(KEY_RUN_HOUR, 6)
        val runMinute = prefs.getInt(KEY_RUN_MINUTE, 30)
        val quizEnabled = prefs.getBoolean(KEY_QUIZ_ENABLED, true)
        val quizDay = prefs.getInt(KEY_QUIZ_DAY, Calendar.SUNDAY)
        val quizHour = prefs.getInt(KEY_QUIZ_HOUR, 9)
        val quizMinute = prefs.getInt(KEY_QUIZ_MINUTE, 0)

        scheduleRunReminder(context, runEnabled, runHour, runMinute)
        scheduleWeeklyQuizReminder(context, quizEnabled, quizDay, quizHour, quizMinute)
    }

    fun getCachedAthleteName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ATHLETE_NAME, "")?.trim().orEmpty()
    }

    private fun scheduleRunReminder(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_RUN_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQ_RUN_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!enabled) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun scheduleWeeklyQuizReminder(
        context: Context,
        enabled: Boolean,
        dayOfWeek: Int,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_QUIZ_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQ_QUIZ_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!enabled) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7L,
            pendingIntent
        )
    }

    fun showRunNotification(context: Context, athleteName: String) {
        if (!hasNotificationPermission(context)) return
        ensureChannel(context)

        val displayName = athleteName.ifBlank { "Atlet" }
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_TAB, TAB_DASHBOARD)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIF_ID_RUN,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Waktunya Lari Rutin, $displayName!")
            .setContentText("Ayo mulai pemanasan dan lari hari ini untuk menjaga kebugaran jantung & capai target langkahmu.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Ayo mulai pemanasan dan lari hari ini untuk menjaga kebugaran jantung & capai target langkahmu di PulseRun."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIF_ID_RUN, notification)
    }

    fun showWeeklyQuizNotification(context: Context, athleteName: String) {
        if (!hasNotificationPermission(context)) return
        ensureChannel(context)

        val displayName = athleteName.ifBlank { "Atlet" }
        val weekOfYear = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
        val cycleWeek = ((weekOfYear - 1) % 4) + 1
        val theme = WeeklyQuizCatalog.themeForWeek(cycleWeek)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_TAB, TAB_WEEKLY_QUIZ)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIF_ID_QUIZ,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Kuis Kesehatan ${theme.weekLabel} Menunggumu!")
            .setContentText("Hai $displayName, tema minggu ini: ${theme.title}. Jawab kuisnya & raih poin kebugaran!")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Hai $displayName, tema kuis minggu ini adalah \"${theme.title}\". Uji wawasan kesehatanmu dan kumpulkan poin bonus streak!"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIF_ID_QUIZ, notification)
    }
}
