package com.example.sensors

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class RunningTrackerService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START, ACTION_UPDATE -> {
                val elapsed = intent.getStringExtra(EXTRA_TIME) ?: "00:00:00"
                val distance = intent.getStringExtra(EXTRA_DISTANCE) ?: "0.00 km"
                val pace = intent.getStringExtra(EXTRA_PACE) ?: "0'00\"/km"
                val paused = intent.getBooleanExtra(EXTRA_PAUSED, false)

                ensureChannel()
                val notification = buildNotification(elapsed, distance, pace, paused)

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(
                            NOTIFICATION_ID,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                        )
                    } else {
                        startForeground(NOTIFICATION_ID, notification)
                    }
                } catch (_: SecurityException) {
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Pelacakan Lari GPS",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Menampilkan waktu, jarak, dan pace lari secara real-time."
                    setShowBadge(false)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun buildNotification(
        elapsed: String,
        distance: String,
        pace: String,
        paused: Boolean
    ): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusTitle = if (paused) "PulseRun • Lari Dijeda" else "PulseRun • Melacak Lari GPS"
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(statusTitle)
            .setContentText("Waktu $elapsed  •  $distance  •  Pace $pace")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "pulserun_tracking"
        private const val NOTIFICATION_ID = 4102

        const val ACTION_START = "com.example.action.TRACKING_START"
        const val ACTION_UPDATE = "com.example.action.TRACKING_UPDATE"
        const val ACTION_STOP = "com.example.action.TRACKING_STOP"

        const val EXTRA_TIME = "extra_time"
        const val EXTRA_DISTANCE = "extra_distance"
        const val EXTRA_PACE = "extra_pace"
        const val EXTRA_PAUSED = "extra_paused"

        fun updateNotification(
            context: Context,
            elapsed: String,
            distance: String,
            pace: String,
            paused: Boolean,
            isStart: Boolean = false
        ) {
            try {
                val intent = Intent(context, RunningTrackerService::class.java).apply {
                    action = if (isStart) ACTION_START else ACTION_UPDATE
                    putExtra(EXTRA_TIME, elapsed)
                    putExtra(EXTRA_DISTANCE, distance)
                    putExtra(EXTRA_PACE, pace)
                    putExtra(EXTRA_PAUSED, paused)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isStart) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, RunningTrackerService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
            } catch (_: Exception) {
            }
        }
    }
}
