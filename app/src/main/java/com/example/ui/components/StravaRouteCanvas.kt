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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoutePoint
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.StravaOrange

@Composable
fun StravaRouteCanvas(
    points: List<RoutePoint>,
    routeLabel: String,
    modifier: Modifier = Modifier
) {
    val safePoints = remember(points) {
        if (points.size >= 2) points else listOf(
            RoutePoint(0.10f, 0.73f),
            RoutePoint(0.25f, 0.25f),
            RoutePoint(0.45f, 0.50f),
            RoutePoint(0.73f, 0.31f),
            RoutePoint(0.90f, 0.65f)
        )
    }
    val routePath = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(148.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(18.dp))
            .semantics { contentDescription = "Peta pratinjau rute $routeLabel" }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 24.dp.toPx()
            val gridColor = Color.White.copy(alpha = 0.06f)
            var x = gridStep
            while (x < size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += gridStep
            }
            var y = gridStep
            while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += gridStep
            }

            routePath.reset()
            fun mapPt(pt: RoutePoint) = Offset(
                pt.xFraction.coerceIn(0.05f, 0.95f) * size.width,
                pt.yFraction.coerceIn(0.10f, 0.88f) * size.height
            )

            val firstPt = mapPt(safePoints.first())
            var prev = firstPt
            routePath.moveTo(firstPt.x, firstPt.y)
            for (i in 1 until safePoints.size) {
                val curr = mapPt(safePoints[i])
                val midX = (prev.x + curr.x) / 2f
                val midY = (prev.y + curr.y) / 2f
                routePath.quadraticTo(prev.x, prev.y, midX, midY)
                prev = curr
            }
            routePath.lineTo(prev.x, prev.y)

            // Soft glow
            drawPath(
                path = routePath,
                color = StravaOrange.copy(alpha = 0.28f),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Main GPS trace
            drawPath(
                path = routePath,
                color = StravaOrange,
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Start & Finish pins
            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = firstPt)
            drawCircle(color = EmeraldAccent, radius = 4.dp.toPx(), center = firstPt)
            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = prev)
            drawCircle(color = StravaOrange, radius = 4.dp.toPx(), center = prev)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.70f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = routeLabel,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
