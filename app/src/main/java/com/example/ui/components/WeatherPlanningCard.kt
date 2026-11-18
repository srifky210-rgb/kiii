package com.example.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.LocalWeatherState
import com.example.data.remote.RunWeatherSeverity
import com.example.ui.theme.CalorieAmber
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.HeartRose
import com.example.ui.theme.LocalPulseRunColors
import com.example.ui.theme.StepBlue
import com.example.ui.theme.StravaOrange
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WeatherPlanningCard(
    weather: LocalWeatherState,
    hasLocationPermission: Boolean,
    onRefreshWeather: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    val adviceTint = when (weather.runAdvice.severity) {
        RunWeatherSeverity.IDEAL -> EmeraldAccent
        RunWeatherSeverity.CAUTION -> CalorieAmber
        RunWeatherSeverity.WARNING -> HeartRose
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(palette.cardBackground)
            .border(1.dp, palette.cardBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("weather_planning_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Row: Location Pill + GPS Prompt + Refresh Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(StepBlue.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = "Lokasi cuaca",
                        tint = StepBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CUACA LOKAL • OPEN-METEO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = StepBlue,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = weather.locationLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("weather_location_text")
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!hasLocationPermission) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(StravaOrange.copy(alpha = 0.14f))
                            .clickable(onClick = onRequestLocationPermission)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("weather_enable_gps_chip")
                    ) {
                        Text(
                            text = "Izinkan GPS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StravaOrange
                        )
                    }
                }
                IconButton(
                    onClick = onRefreshWeather,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("refresh_weather_button")
                ) {
                    if (weather.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = StepBlue
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Perbarui cuaca lokal",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        when {
            weather.isLoading && !weather.hasLoadedData -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.subtleSurface)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.2.dp,
                        color = StepBlue
                    )
                    Text(
                        text = "Mengambil data suhu & prakiraan hujan lokal...",
                        fontSize = 12.sp,
                        color = palette.textSecondary
                    )
                }
            }

            weather.errorMessage != null && !weather.hasLoadedData -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.subtleSurface)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = weather.errorMessage,
                        fontSize = 12.sp,
                        color = palette.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = onRefreshWeather,
                        modifier = Modifier.testTag("retry_weather_button")
                    ) {
                        Text("Coba Lagi", fontWeight = FontWeight.Bold)
                    }
                }
            }

            else -> {
                // Temperature & Precipitation Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = weather.conditionEmoji,
                            fontSize = 34.sp
                        )
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${weather.temperatureC.roundToInt()}°C",
                                    fontFamily = FredokaFontFamily,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary,
                                    style = TextStyle(fontFeatureSettings = "tnum"),
                                    modifier = Modifier.testTag("weather_temp_text")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Terasa ${weather.apparentTemperatureC.roundToInt()}°C",
                                    fontSize = 11.sp,
                                    color = palette.textSecondary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Text(
                                text = "${weather.conditionLabel} • Lembap ${weather.humidityPct}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = palette.textSecondary
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WaterDrop,
                                contentDescription = "Peluang & curah hujan",
                                tint = StepBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "%d%% (%.1f mm)",
                                    weather.precipProbabilityPct,
                                    weather.precipitationMm
                                ),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StepBlue,
                                style = TextStyle(fontFeatureSettings = "tnum"),
                                modifier = Modifier.testTag("weather_precip_text")
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Air,
                                contentDescription = "Kecepatan angin",
                                tint = palette.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = String.format(Locale.US, "%.0f km/j", weather.windSpeedKmh),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.textSecondary,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }
                    }
                }

                // Running Advice Banner based on Temperature & Precipitation
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(adviceTint.copy(alpha = 0.12f))
                        .border(1.dp, adviceTint.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = weather.runAdvice.headline,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = adviceTint
                    )
                    Text(
                        text = weather.runAdvice.detail,
                        fontSize = 11.sp,
                        color = palette.textPrimary.copy(alpha = 0.85f),
                        lineHeight = 15.sp
                    )
                }

                // Hourly Planning Forecast Strip
                if (weather.hourlySlots.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        weather.hourlySlots.forEach { slot ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(palette.subtleSurface)
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = slot.hourLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.textSecondary,
                                    style = TextStyle(fontFeatureSettings = "tnum")
                                )
                                Text(text = slot.emoji, fontSize = 16.sp)
                                Text(
                                    text = "${slot.temperatureC.roundToInt()}°",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary,
                                    style = TextStyle(fontFeatureSettings = "tnum")
                                )
                                Text(
                                    text = "💧${slot.precipProbabilityPct}%",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (slot.precipProbabilityPct >= 40) StepBlue else palette.textSecondary,
                                    style = TextStyle(fontFeatureSettings = "tnum")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
