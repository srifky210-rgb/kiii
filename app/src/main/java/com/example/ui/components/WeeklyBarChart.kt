package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyMetricEntity
import com.example.ui.theme.LocalPulseRunColors
import com.example.ui.theme.StepBlue
import com.example.ui.theme.StravaOrange
import java.util.Locale

@Composable
fun WeeklyBarChart(
    metrics: List<DailyMetricEntity>,
    todayDateKey: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    val items = metrics.takeLast(7)
    val maxSteps = (items.maxOfOrNull { it.steps } ?: 10000).coerceAtLeast(11000)
    var selectedIdx by remember(items.size) { mutableIntStateOf(items.lastIndex.coerceAtLeast(0)) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(148.dp)
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        items.forEachIndexed { idx, day ->
            val isToday = day.dateKey == todayDateKey || idx == items.lastIndex
            val isSelected = idx == selectedIdx
            val targetFraction = (day.steps.toFloat() / maxSteps.toFloat()).coerceIn(0.08f, 1f)
            val animatedFraction by animateFloatAsState(
                targetValue = targetFraction,
                animationSpec = tween(durationMillis = 500),
                label = "bar_$idx"
            )
            val barBg = if (palette.isDark) MaterialTheme.colorScheme.background else palette.subtleSurface
            val barColor = if (isToday) StravaOrange else StepBlue

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { selectedIdx = idx },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = String.format(Locale.US, "%.1fk", day.steps / 1000f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected || isToday) barColor else palette.textSecondary.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(92.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(barBg)
                        .padding(2.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(animatedFraction)
                            .clip(RoundedCornerShape(8.dp))
                            .background(barColor)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = day.dayLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isToday) StravaOrange else palette.textSecondary
                )
            }
        }
    }
}
