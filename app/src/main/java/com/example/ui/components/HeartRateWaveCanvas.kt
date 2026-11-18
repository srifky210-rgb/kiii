package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.ui.theme.HeartRose
import com.example.ui.theme.LocalPulseRunColors

@Composable
fun HeartRateWaveCanvas(
    samples: List<Float>,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    val containerBg = if (palette.isDark) Color(0x990B0F19) else Color(0x99FFFFFF)
    val borderColor = if (palette.isDark) Color(0xFF1E293B) else Color(0xFFFFE4E6)

    // Reuse Path instances across frames to avoid GC churn during live waveform updates
    val strokePath = remember { Path() }
    val fillPath = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerBg)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .semantics { contentDescription = "Grafik gelombang detak jantung langsung" }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (samples.size < 2) return@Canvas
            strokePath.reset()
            fillPath.reset()

            val stepX = size.width / (samples.size - 1).toFloat()
            val verticalPad = 6f
            val usableHeight = (size.height - verticalPad * 2f).coerceAtLeast(8f)

            fun pointY(bpm: Float): Float {
                val normalized = ((bpm - 50f) / 140f).coerceIn(0f, 1f)
                return size.height - (normalized * usableHeight) - verticalPad
            }

            var prevX = 0f
            var prevY = pointY(samples.first())
            strokePath.moveTo(prevX, prevY)
            fillPath.moveTo(prevX, prevY)

            for (index in 1 until samples.size) {
                val x = index * stepX
                val y = pointY(samples[index])
                val midX = (prevX + x) * 0.5f
                val midY = (prevY + y) * 0.5f
                strokePath.quadraticTo(prevX, prevY, midX, midY)
                fillPath.quadraticTo(prevX, prevY, midX, midY)
                prevX = x
                prevY = y
            }
            strokePath.lineTo(prevX, prevY)
            fillPath.lineTo(prevX, prevY)

            fillPath.lineTo(size.width, size.height)
            fillPath.lineTo(0f, size.height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(HeartRose.copy(alpha = 0.28f), Color.Transparent),
                    startY = 0f,
                    endY = size.height
                )
            )
            drawPath(
                path = strokePath,
                color = HeartRose,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
            // Leading pulse dot
            drawCircle(
                color = HeartRose.copy(alpha = 0.28f),
                radius = 5.5.dp.toPx(),
                center = Offset(prevX, prevY)
            )
            drawCircle(
                color = HeartRose,
                radius = 3.dp.toPx(),
                center = Offset(prevX, prevY)
            )
        }
    }
}
