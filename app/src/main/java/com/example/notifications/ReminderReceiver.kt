package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val athleteName = ReminderScheduler.getCachedAthleteName(context)

        when (action) {
            ReminderScheduler.ACTION_RUN_REMINDER -> {
                ReminderScheduler.showRunNotification(context, athleteName)
            }
            ReminderScheduler.ACTION_QUIZ_REMINDER -> {
                ReminderScheduler.showWeeklyQuizNotification(context, athleteName)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                ReminderScheduler.rescheduleFromCache(context)
            }
        }
    }
}
