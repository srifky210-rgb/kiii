package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeeklyTrendPoint
import com.example.data.model.WeeklyTrendSummary
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.LocalPulseRunColors
import com.example.ui.theme.PenjasCyan
import com.example.ui.theme.PenjasTealVibrant
import com.example.ui.theme.StravaOrange
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

enum class TrendChartMode(val label: String) {
    COMBINED("Gabungan"),
    DISTANCE("Jarak"),
    SPEED("Kecepatan")
}

@Composable
fun WeeklyRunTrendChartCard(
    trend: WeeklyTrendSummary,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    val points = trend.points
    var chartMode by rememberSaveable { mutableStateOf(TrendChartMode.COMBINED) }
    var selectedIdx by remember(points.size) {
        mutableIntStateOf(points.indexOfLast { it.isToday }.takeIf { it >= 0 } ?: points.lastIndex.coerceAtLeast(0))
    }

    val safeIndex = selectedIdx.coerceIn(0, (points.size - 1).coerceAtLeast(0))
    val activePoint = points.getOrNull(safeIndex)
    val speedColor = if (palette.isDark) PenjasCyan else PenjasTealVibrant
    val distanceColor = StravaOrange

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(palette.cardBackground)
            .border(1.dp, palette.cardBorder, RoundedCornerShape(24.dp))
            .padding(16.dp)
            .testTag("weekly_run_trend_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header + Metric Mode Segmented Toggle
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tren Jarak & Kecepatan Mingguan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "Ketuk atau geser grafik untuk detail harian",
                        fontSize = 11.sp,
                        color = palette.textSecondary
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.subtleSurface)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TrendChartMode.entries.forEach { mode ->
                    val selected = chartMode == mode
                    val bg by animateColorAsState(
                        targetValue = if (selected) palette.cardBackground else Color.Transparent,
                        animationSpec = tween(180),
                        label = "trend_mode_bg"
                    )
                    val txtColor by animateColorAsState(
                        targetValue = if (selected) distanceColor else palette.textSecondary,
                        animationSpec = tween(180),
                        label = "trend_mode_txt"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(bg)
                            .clickable { chartMode = mode }
                            .padding(vertical = 8.dp)
                            .testTag("trend_mode_${mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = txtColor
                        )
                    }
                }
            }
        }

        // Interactive Tooltip Banner (Recharts-style active point inspector)
        if (activePoint != null) {
            TrendTooltipBanner(
                point = activePoint,
                distanceColor = distanceColor,
                speedColor = speedColor
            )
        }

        // Native Canvas Chart (Gridlines, Axes, Bezier Area/Line, Bars, Active Crosshair)
        if (points.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.subtleSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada data tren minggu ini",
                    fontSize = 12.sp,
                    color = palette.textSecondary
                )
            }
        } else {
            WeeklyTrendCanvas(
                points = points,
                selectedIndex = safeIndex,
                mode = chartMode,
                distanceColor = distanceColor,
                speedColor = speedColor,
                onSelectIndex = { selectedIdx = it }
            )
        }

        // Legend Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (chartMode != TrendChartMode.SPEED) {
                LegendChip(
                    color = distanceColor,
                    label = if (chartMode == TrendChartMode.COMBINED) "Jarak Harian (km)" else "Tren Jarak (km)"
                )
            }
            if (chartMode != TrendChartMode.DISTANCE) {
                LegendChip(
                    color = speedColor,
                    label = "Kecepatan Rata-rata (km/j)"
                )
            }
        }

        HorizontalDivider(color = palette.cardBorder.copy(alpha = 0.6f))

        // Summary Stats Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TrendSummaryStat(
                label = "Total 7 Hari",
                value = String.format(Locale.US, "%.1f km", trend.totalDistanceKm),
                accent = distanceColor,
                modifier = Modifier.weight(1f)
            )
            TrendSummaryStat(
                label = "Rata-rata Kec.",
                value = String.format(Locale.US, "%.1f km/j", trend.avgSpeedKmh),
                accent = speedColor,
                modifier = Modifier.weight(1f)
            )
            TrendSummaryStat(
                label = "Puncak Kec.",
                value = String.format(Locale.US, "%.1f km/j", trend.peakSpeedKmh),
                accent = EmeraldAccent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TrendTooltipBanner(
    point: WeeklyTrendPoint,
    distanceColor: Color,
    speedColor: Color
) {
    val palette = LocalPulseRunColors.current
    val dayTitle = if (point.isToday) "${point.dayLabel} (Hari Ini)" else point.dayLabel
    val sourceLabel = when {
        point.workoutCount > 0 -> "${point.workoutCount} sesi lari"
        point.distanceKm > 0.05 -> "Estimasi harian"
        else -> "Belum ada aktivitas"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.subtleSurface)
            .border(1.dp, palette.cardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
            .testTag("weekly_trend_tooltip"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$dayTitle • ${point.dateKey.takeLast(5)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sourceLabel,
                fontSize = 10.sp,
                color = palette.textSecondary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.US, "%.2f km", point.distanceKm),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = distanceColor,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
                Text(
                    text = "Jarak",
                    fontSize = 9.sp,
                    color = palette.textSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (point.speedKmh > 0.1) {
                        String.format(Locale.US, "%.1f km/j", point.speedKmh)
                    } else {
                        "–"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = speedColor,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
                Text(
                    text = if (point.speedKmh > 0.1) "${point.paceDisplay}/km" else "Kecepatan",
                    fontSize = 9.sp,
                    color = palette.textSecondary,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }
    }
}

@Composable
private fun WeeklyTrendCanvas(
    points: List<WeeklyTrendPoint>,
    selectedIndex: Int,
    mode: TrendChartMode,
    distanceColor: Color,
    speedColor: Color,
    onSelectIndex: (Int) -> Unit
) {
    val palette = LocalPulseRunColors.current
    val textMeasurer = rememberTextMeasurer()

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(550),
        label = "chart_reveal"
    )

    val linePath = remember { Path() }
    val fillPath = remember { Path() }
    val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f) }

    val maxDist = remember(points) {
        val peak = points.maxOfOrNull { it.distanceKm } ?: 5.0
        ceil((peak * 1.18).coerceAtLeast(4.0)).toFloat()
    }
    val maxSpeed = remember(points) {
        val peak = points.maxOfOrNull { it.speedKmh } ?: 10.0
        ceil((peak * 1.18).coerceAtLeast(10.0)).toFloat()
    }

    val gridColor = palette.cardBorder.copy(alpha = 0.7f)
    val axisLabelStyle = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = palette.textSecondary,
        fontFeatureSettings = "tnum"
    )
    val todayLabelStyle = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = StravaOrange
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .pointerInput(points.size) {
                detectTapGestures { offset ->
                    val leftPad = 34.dp.toPx()
                    val rightPad = if (mode == TrendChartMode.COMBINED) 34.dp.toPx() else 12.dp.toPx()
                    val chartW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                    val relX = ((offset.x - leftPad) / chartW).coerceIn(0f, 1f)
                    val idx = (relX * (points.size - 1)).roundToInt().coerceIn(0, points.lastIndex)
                    onSelectIndex(idx)
                }
            }
            .pointerInput(points.size) {
                detectHorizontalDragGestures { change, _ ->
                    val leftPad = 34.dp.toPx()
                    val rightPad = if (mode == TrendChartMode.COMBINED) 34.dp.toPx() else 12.dp.toPx()
                    val chartW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                    val relX = ((change.position.x - leftPad) / chartW).coerceIn(0f, 1f)
                    val idx = (relX * (points.size - 1)).roundToInt().coerceIn(0, points.lastIndex)
                    onSelectIndex(idx)
                }
            }
            .testTag("weekly_trend_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val leftPad = 34.dp.toPx()
            val rightPad = if (mode == TrendChartMode.COMBINED) 34.dp.toPx() else 12.dp.toPx()
            val topPad = 14.dp.toPx()
            val bottomPad = 26.dp.toPx()

            val chartW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
            val chartH = (size.height - topPad - bottomPad).coerceAtLeast(1f)
            val stepX = if (points.size > 1) chartW / (points.size - 1) else chartW

            // 1. Horizontal Cartesian gridlines + Y-axis labels
            val gridSteps = 3
            for (i in 0..gridSteps) {
                val frac = i / gridSteps.toFloat()
                val y = topPad + chartH * (1f - frac)
                drawLine(
                    color = gridColor,
                    start = Offset(leftPad, y),
                    end = Offset(leftPad + chartW, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = if (i == 0) null else dashEffect
                )

                val leftVal = if (mode == TrendChartMode.SPEED) maxSpeed * frac else maxDist * frac
                val leftText = if (mode == TrendChartMode.SPEED) {
                    "${leftVal.roundToInt()}"
                } else {
                    String.format(Locale.US, "%.0fk", leftVal).replace("k", "")
                }
                val measuredLeft = textMeasurer.measure(leftText, axisLabelStyle)
                drawText(
                    textLayoutResult = measuredLeft,
                    topLeft = Offset(
                        x = (leftPad - measuredLeft.size.width - 6.dp.toPx()).coerceAtLeast(0f),
                        y = (y - measuredLeft.size.height / 2f).coerceAtLeast(0f)
                    )
                )

                if (mode == TrendChartMode.COMBINED) {
                    val speedTick = "${(maxSpeed * frac).roundToInt()}"
                    val measuredRight = textMeasurer.measure(speedTick, axisLabelStyle.copy(color = speedColor))
                    drawText(
                        textLayoutResult = measuredRight,
                        topLeft = Offset(
                            x = leftPad + chartW + 6.dp.toPx(),
                            y = (y - measuredRight.size.height / 2f).coerceAtLeast(0f)
                        )
                    )
                }
            }

            // 2. X-Axis Day Labels
            points.forEachIndexed { idx, pt ->
                val cx = leftPad + idx * stepX
                val style = if (idx == selectedIndex || pt.isToday) todayLabelStyle else axisLabelStyle
                val measured = textMeasurer.measure(pt.dayLabel, style)
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(
                        x = (cx - measured.size.width / 2f).coerceIn(0f, size.width - measured.size.width),
                        y = topPad + chartH + 8.dp.toPx()
                    )
                )
            }

            // 3. Selected Vertical Cursor Guide
            val selectedX = leftPad + selectedIndex * stepX
            drawLine(
                color = palette.textSecondary.copy(alpha = 0.4f),
                start = Offset(selectedX, topPad),
                end = Offset(selectedX, topPad + chartH),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = dashEffect
            )

            // 4. Distance Bars (in COMBINED mode)
            if (mode == TrendChartMode.COMBINED) {
                val barW = (stepX * 0.44f).coerceIn(12.dp.toPx(), 24.dp.toPx())
                points.forEachIndexed { idx, pt ->
                    val cx = leftPad + idx * stepX
                    val barFrac = (pt.distanceKm.toFloat() / maxDist).coerceIn(0f, 1f) * animProgress
                    val barH = chartH * barFrac
                    val isSelected = idx == selectedIndex
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                distanceColor.copy(alpha = if (isSelected) 0.88f else 0.45f),
                                distanceColor.copy(alpha = if (isSelected) 0.42f else 0.16f)
                            )
                        ),
                        topLeft = Offset(cx - barW / 2f, topPad + chartH - barH),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            // 5. Smooth Area + Line Curve (Distance in DISTANCE mode, Speed in COMBINED & SPEED modes)
            val useSpeedCurve = mode != TrendChartMode.DISTANCE
            val curveColor = if (useSpeedCurve) speedColor else distanceColor
            val maxAxis = if (useSpeedCurve) maxSpeed else maxDist

            val curveCoords = points.mapIndexed { idx, pt ->
                val raw = if (useSpeedCurve) pt.speedKmh.toFloat() else pt.distanceKm.toFloat()
                val frac = (raw / maxAxis).coerceIn(0f, 1f) * animProgress
                Offset(
                    x = leftPad + idx * stepX,
                    y = topPad + chartH * (1f - frac)
                )
            }

            if (curveCoords.size >= 2) {
                linePath.reset()
                fillPath.reset()

                linePath.moveTo(curveCoords.first().x, curveCoords.first().y)
                fillPath.moveTo(curveCoords.first().x, topPad + chartH)
                fillPath.lineTo(curveCoords.first().x, curveCoords.first().y)

                for (i in 0 until curveCoords.lastIndex) {
                    val p0 = curveCoords[i]
                    val p1 = curveCoords[i + 1]
                    val midX = (p0.x + p1.x) / 2f
                    linePath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                    fillPath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                }

                fillPath.lineTo(curveCoords.last().x, topPad + chartH)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            curveColor.copy(alpha = if (mode == TrendChartMode.COMBINED) 0.18f else 0.28f),
                            curveColor.copy(alpha = 0.01f)
                        ),
                        startY = topPad,
                        endY = topPad + chartH
                    )
                )

                drawPath(
                    path = linePath,
                    color = curveColor,
                    style = Stroke(
                        width = 2.6.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // 6. Data Nodes & Glowing Active Point Halo
            curveCoords.forEachIndexed { idx, center ->
                val isSelected = idx == selectedIndex
                if (isSelected) {
                    drawCircle(
                        color = curveColor.copy(alpha = 0.22f),
                        radius = 9.dp.toPx(),
                        center = center
                    )
                }
                drawCircle(
                    color = palette.cardBackground,
                    radius = if (isSelected) 5.5.dp.toPx() else 3.5.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = curveColor,
                    radius = if (isSelected) 4.dp.toPx() else 2.5.dp.toPx(),
                    center = center
                )
            }
        }
    }
}

@Composable
private fun LegendChip(
    color: Color,
    label: String
) {
    val palette = LocalPulseRunColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = palette.textSecondary
        )
    }
}

@Composable
private fun TrendSummaryStat(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = palette.textSecondary
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accent,
            style = TextStyle(fontFeatureSettings = "tnum")
        )
    }
}
