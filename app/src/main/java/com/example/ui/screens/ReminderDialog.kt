package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserPreferences
import com.example.ui.theme.CalorieAmber
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.LocalPulseRunColors
import com.example.ui.theme.PenjasTealVibrant
import com.example.ui.theme.StravaOrange
import java.util.Calendar
import java.util.Locale

@Composable
fun ReminderSettingsDialog(
    preferences: UserPreferences,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onUpdateRunReminder: (enabled: Boolean, hour: Int, minute: Int) -> Unit,
    onUpdateQuizReminder: (enabled: Boolean, dayOfWeek: Int, hour: Int, minute: Int) -> Unit,
    onSendTestRunNotification: () -> Unit,
    onSendTestQuizNotification: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalPulseRunColors.current

    val runTimeSlots = listOf(
        6 to 0,
        6 to 30,
        16 to 30,
        17 to 30
    )

    val quizDays = listOf(
        Calendar.MONDAY to "Senin",
        Calendar.WEDNESDAY to "Rabu",
        Calendar.SATURDAY to "Sabtu",
        Calendar.SUNDAY to "Minggu"
    )

    val quizTimeSlots = listOf(
        8 to 0,
        9 to 0,
        19 to 0
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pengingat & Notifikasi",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!hasNotificationPermission) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CalorieAmber.copy(alpha = 0.14f))
                            .border(1.dp, CalorieAmber.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Izin notifikasi belum aktif agar pengingat dapat tampil di bilah status.",
                            fontSize = 12.sp,
                            color = palette.textPrimary
                        )
                        Button(
                            onClick = onRequestNotificationPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CalorieAmber,
                                contentColor = Color(0xFF451A03)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("grant_notification_permission_button")
                        ) {
                            Text("Izinkan Notifikasi", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // 1. Daily Run Reminder Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.subtleSurface)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pengingat Lari Rutin",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "Harian • Pukul %02d:%02d",
                                    preferences.runReminderHour,
                                    preferences.runReminderMinute
                                ),
                                fontSize = 11.sp,
                                color = StravaOrange
                            )
                        }
                        Switch(
                            checked = preferences.isRunReminderEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateRunReminder(
                                    enabled,
                                    preferences.runReminderHour,
                                    preferences.runReminderMinute
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = StravaOrange
                            ),
                            modifier = Modifier.testTag("run_reminder_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        runTimeSlots.forEach { (h, m) ->
                            val selected = preferences.runReminderHour == h && preferences.runReminderMinute == m
                            ScheduleChip(
                                label = String.format(Locale.US, "%02d:%02d", h, m),
                                selected = selected,
                                accent = StravaOrange,
                                onClick = { onUpdateRunReminder(true, h, m) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onSendTestRunNotification,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("test_run_notification_button")
                    ) {
                        Text("Kirim Uji Coba Pengingat Lari", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                HorizontalDivider(color = palette.cardBorder)

                // 2. Weekly Health Quiz Reminder Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.subtleSurface)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pengingat Kuis Mingguan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                            val dayName = quizDays.firstOrNull { it.first == preferences.quizReminderDayOfWeek }?.second ?: "Minggu"
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "Tiap %s • Pukul %02d:%02d",
                                    dayName,
                                    preferences.quizReminderHour,
                                    preferences.quizReminderMinute
                                ),
                                fontSize = 11.sp,
                                color = PenjasTealVibrant
                            )
                        }
                        Switch(
                            checked = preferences.isQuizReminderEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateQuizReminder(
                                    enabled,
                                    preferences.quizReminderDayOfWeek,
                                    preferences.quizReminderHour,
                                    preferences.quizReminderMinute
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PenjasTealVibrant
                            ),
                            modifier = Modifier.testTag("quiz_reminder_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quizDays.forEach { (dayConst, label) ->
                            val selected = preferences.quizReminderDayOfWeek == dayConst
                            ScheduleChip(
                                label = label,
                                selected = selected,
                                accent = PenjasTealVibrant,
                                onClick = {
                                    onUpdateQuizReminder(
                                        true,
                                        dayConst,
                                        preferences.quizReminderHour,
                                        preferences.quizReminderMinute
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quizTimeSlots.forEach { (h, m) ->
                            val selected = preferences.quizReminderHour == h && preferences.quizReminderMinute == m
                            ScheduleChip(
                                label = String.format(Locale.US, "%02d:%02d", h, m),
                                selected = selected,
                                accent = EmeraldAccent,
                                onClick = {
                                    onUpdateQuizReminder(
                                        true,
                                        preferences.quizReminderDayOfWeek,
                                        h,
                                        m
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onSendTestQuizNotification,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("test_quiz_notification_button")
                    ) {
                        Text("Kirim Uji Coba Kuis Mingguan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun ScheduleChip(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) accent.copy(alpha = 0.16f) else palette.cardBackground)
            .border(1.dp, if (selected) accent else palette.cardBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) accent else palette.textSecondary
        )
    }
}
