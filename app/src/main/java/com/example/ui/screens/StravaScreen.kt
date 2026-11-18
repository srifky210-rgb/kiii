package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WorkoutActivityEntity
import com.example.data.model.RoutePoint
import com.example.ui.components.StravaRouteCanvas
import com.example.ui.components.pressScale
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.LocalPulseRunColors
import com.example.ui.theme.StravaOrange
import com.example.ui.theme.StravaOrangeDark
import com.example.ui.viewmodel.PulseRunUiState
import java.util.Locale

@Composable
fun StravaScreen(
    uiState: PulseRunUiState,
    onToggleAutoSync: (Boolean) -> Unit,
    onToggleWorkoutSync: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    val context = LocalContext.current
    val workouts = uiState.workouts
    val latestWorkout = workouts.firstOrNull()
    val monthlyDistance = 63.28 + workouts.sumOf { it.distanceKm }
    val totalActivities = 13 + workouts.size
    val totalElevation = 380 + (workouts.size * 32)

    val routePoints = remember(latestWorkout?.routePointsEncoded) {
        latestWorkout?.routePointsEncoded
            ?.split(";")
            ?.mapNotNull { pair ->
                val parts = pair.split(",")
                if (parts.size == 2) {
                    val x = parts[0].toFloatOrNull()
                    val y = parts[1].toFloatOrNull()
                    if (x != null && y != null) RoutePoint(x, y) else null
                } else null
            } ?: emptyList()
    }

    val routeLabel = if (latestWorkout != null) {
        String.format(Locale.US, "%s • %.2f km", latestWorkout.routeName, latestWorkout.distanceKm)
    } else {
        "Summarecon Loop • 5.12 km"
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Strava Gradient Connection Card
            item(key = "strava_hero", contentType = "hero") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(Brush.horizontalGradient(listOf(StravaOrange, Color(0xFFEA580C))))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bolt,
                                    contentDescription = "Strava",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Koneksi Strava",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${uiState.preferences.athleteName} • ${if (uiState.preferences.isStravaAutoSync) "Auto-Sync Aktif" else "Dijeda"}",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Switch(
                            checked = uiState.preferences.isStravaAutoSync,
                            onCheckedChange = onToggleAutoSync,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldAccent,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Black.copy(alpha = 0.30f)
                            ),
                            modifier = Modifier.testTag("strava_autosync_switch")
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.22f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StravaStatColumn(
                            label = "Jarak Bulan Ini",
                            value = String.format(Locale.US, "%.1f km", monthlyDistance)
                        )
                        StravaStatColumn(
                            label = "Total Aktivitas",
                            value = "$totalActivities Lari"
                        )
                        StravaStatColumn(
                            label = "Elevasi",
                            value = "$totalElevation m"
                        )
                    }
                }
            }

            // 2. Route Preview & Native Share/Export Card
            item(key = "strava_route_card", contentType = "route") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(palette.cardBackground)
                        .border(1.dp, palette.cardBorder, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pratinjau Rute & Segmen Terakhir",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Segmen PR 🏆",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StravaOrange
                        )
                    }

                    StravaRouteCanvas(
                        points = routePoints,
                        routeLabel = routeLabel
                    )

                    val shareInteraction = remember { MutableInteractionSource() }
                    Button(
                        onClick = {
                            val summary = if (latestWorkout != null) {
                                String.format(
                                    Locale.US,
                                    "PulseRun 🏃‍♂️ %s: %.2f km | %d kkal | %d BPM | Pace %s/km",
                                    latestWorkout.title,
                                    latestWorkout.distanceKm,
                                    latestWorkout.calories,
                                    latestWorkout.avgBpm,
                                    latestWorkout.paceDisplay
                                )
                            } else {
                                "PulseRun 🏃‍♂️ Lari Pagi Santai: 5.12 km | 410 kkal | 138 BPM"
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Aktivitas Lari PulseRun")
                                putExtra(Intent.EXTRA_TEXT, summary)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Aktivitas Lari"))
                        },
                        interactionSource = shareInteraction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StravaOrange.copy(alpha = 0.14f),
                            contentColor = StravaOrange
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .pressScale(shareInteraction)
                            .testTag("share_activity_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bagikan Ringkasan Lari",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3. History Header
            item(key = "strava_history_header", contentType = "header") {
                Text(
                    text = "Riwayat Aktivitas Tersinkronisasi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            items(items = workouts, key = { it.id }, contentType = { "workout_item" }) { workout ->
                WorkoutHistoryCard(
                    workout = workout,
                    onToggleSync = { onToggleWorkoutSync(workout.id, workout.syncedToStrava) }
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun StravaStatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.80f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

@Composable
private fun WorkoutHistoryCard(
    workout: WorkoutActivityEntity,
    onToggleSync: () -> Unit
) {
    val palette = LocalPulseRunColors.current
    val badgeBg = if (workout.syncedToStrava) EmeraldAccent.copy(alpha = 0.14f) else StravaOrange.copy(alpha = 0.14f)
    val badgeTextColor = if (workout.syncedToStrava) EmeraldAccent else StravaOrangeDark

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.cardBackground)
            .border(1.dp, palette.cardBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("workout_history_${workout.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StravaOrange.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ShowChart,
                    contentDescription = null,
                    tint = StravaOrange,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = workout.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Text(
                    text = String.format(
                        Locale.US,
                        "%.2f km • %d kkal • %d BPM",
                        workout.distanceKm,
                        workout.calories,
                        workout.avgBpm
                    ),
                    fontSize = 11.sp,
                    color = palette.textSecondary
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(badgeBg)
                .clickable(onClick = onToggleSync)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (workout.syncedToStrava) "Strava Sync ✓" else "Simpan Lokal",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = badgeTextColor
            )
        }
    }
}
