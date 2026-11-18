package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.data.model.ExerciseAnimationSpec
import com.example.data.model.JointMotion
import com.example.ui.theme.LocalPulseRunColors
import com.example.ui.theme.SunGold
import kotlin.math.PI
import kotlin.math.cos

@Composable
fun PenjasCharacterCanvas(
    spec: ExerciseAnimationSpec,
    isPlaying: Boolean,
    exerciseName: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    var elapsedSec by remember(spec) { mutableFloatStateOf(0f) }

    LaunchedEffect(spec, isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        var lastNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                if (lastNanos != 0L) {
                    val dt = (frameNanos - lastNanos) / 1_000_000_000f
                    elapsedSec += dt.coerceAtMost(0.05f)
                }
                lastNanos = frameNanos
            }
        }
    }

    val bodyColor = if (palette.isDark) Color(0xFFE6F4F1) else Color(0xFF0D3B46)
    val skyTop = if (palette.isDark) Color(0xFF163842) else Color(0xFFCDEFF0)
    val skyBottom = if (palette.isDark) Color(0xFF122C35) else Color(0xFFE9F9F4)
    val grassColor = if (palette.isDark) Color(0xFF164436) else Color(0xFFBFE3C8)
    val smilePath = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Animasi gerakan $exerciseName" },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 240.dp)
                .fillMaxWidth()
                .aspectRatio(200f / 215f)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(skyTop, skyBottom)))
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scaleX = size.width / 200f
                val scaleY = size.height / 215f
                val s = minOf(scaleX, scaleY)
                val offsetX = (size.width - 200f * s) / 2f
                val offsetY = (size.height - 215f * s) / 2f

                // Grass band at bottom 30%
                drawRect(
                    color = grassColor,
                    topLeft = Offset(0f, size.height * 0.70f),
                    size = Size(size.width, size.height * 0.30f)
                )

                translate(left = offsetX, top = offsetY) {
                    // Ground shadow
                    drawOval(
                        color = Color.Black.copy(alpha = 0.18f),
                        topLeft = Offset(45f * s, 197f * s),
                        size = Size(110f * s, 12f * s)
                    )

                    val period = spec.durationSec.coerceAtLeast(0.2f)
                    val bounceT = easeInOutProgress(elapsedSec, period, oppositePhase = false)
                    val figY = lerp(spec.figureOffsetYFrom, spec.figureOffsetYTo, bounceT) * s

                    translate(left = 0f, top = figY) {
                        val hipPivot = Offset(100f * s, 125f * s)
                        val neckPivot = Offset(100f * s, 64f * s)
                        val shoulderPivot = Offset(100f * s, 74f * s)

                        val leftLegDeg = computeAngle(spec.leftLeg, elapsedSec, period)
                        val rightLegDeg = computeAngle(spec.rightLeg, elapsedSec, period)
                        val upperDeg = computeAngle(spec.upperBody, elapsedSec, period)
                        val headDeg = computeAngle(spec.head, elapsedSec, period)
                        val leftArmDeg = computeAngle(spec.leftArm, elapsedSec, period)
                        val rightArmDeg = computeAngle(spec.rightArm, elapsedSec, period)

                        // Left Leg
                        rotate(degrees = leftLegDeg, pivot = hipPivot) {
                            drawLine(
                                color = bodyColor,
                                start = hipPivot,
                                end = Offset(100f * s, 195f * s),
                                strokeWidth = 10f * s,
                                cap = StrokeCap.Round
                            )
                        }

                        // Right Leg
                        rotate(degrees = rightLegDeg, pivot = hipPivot) {
                            drawLine(
                                color = bodyColor,
                                start = hipPivot,
                                end = Offset(100f * s, 195f * s),
                                strokeWidth = 10f * s,
                                cap = StrokeCap.Round
                            )
                        }

                        // Upper body group (torso, head, arms)
                        rotate(degrees = upperDeg, pivot = hipPivot) {
                            // Torso
                            drawLine(
                                color = bodyColor,
                                start = neckPivot,
                                end = hipPivot,
                                strokeWidth = 15f * s,
                                cap = StrokeCap.Round
                            )

                            // Head
                            rotate(degrees = headDeg, pivot = neckPivot) {
                                val headCenter = Offset(100f * s, 42f * s)
                                drawCircle(
                                    color = SunGold,
                                    radius = 19f * s,
                                    center = headCenter
                                )
                                drawCircle(
                                    color = bodyColor,
                                    radius = 19f * s,
                                    center = headCenter,
                                    style = Stroke(width = 4f * s)
                                )
                                // Eyes
                                drawCircle(
                                    color = bodyColor,
                                    radius = 2.5f * s,
                                    center = Offset(93f * s, 40f * s)
                                )
                                drawCircle(
                                    color = bodyColor,
                                    radius = 2.5f * s,
                                    center = Offset(107f * s, 40f * s)
                                )
                                // Smile
                                smilePath.reset()
                                smilePath.moveTo(93f * s, 49f * s)
                                smilePath.quadraticTo(100f * s, 55f * s, 107f * s, 49f * s)
                                drawPath(
                                    path = smilePath,
                                    color = bodyColor,
                                    style = Stroke(width = 3f * s, cap = StrokeCap.Round)
                                )
                            }

                            // Left Arm
                            rotate(degrees = leftArmDeg, pivot = shoulderPivot) {
                                drawLine(
                                    color = bodyColor,
                                    start = shoulderPivot,
                                    end = Offset(100f * s, 132f * s),
                                    strokeWidth = 10f * s,
                                    cap = StrokeCap.Round
                                )
                            }

                            // Right Arm
                            rotate(degrees = rightArmDeg, pivot = shoulderPivot) {
                                drawLine(
                                    color = bodyColor,
                                    start = shoulderPivot,
                                    end = Offset(100f * s, 132f * s),
                                    strokeWidth = 10f * s,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun computeAngle(motion: JointMotion, elapsedSec: Float, periodSec: Float): Float {
    if (motion.fromDeg == motion.toDeg) return motion.fromDeg
    return if (motion.linearLoop) {
        val t = (elapsedSec % periodSec) / periodSec
        lerp(motion.fromDeg, motion.toDeg, t)
    } else {
        val t = easeInOutProgress(elapsedSec, periodSec, motion.oppositePhase)
        lerp(motion.fromDeg, motion.toDeg, t)
    }
}

private fun easeInOutProgress(elapsedSec: Float, periodSec: Float, oppositePhase: Boolean): Float {
    val shifted = if (oppositePhase) elapsedSec + periodSec else elapsedSec
    // Cosine wave mirrors CSS `animation: ... ease-in-out infinite alternate`
    return (0.5f - 0.5f * cos(PI * (shifted / periodSec))).toFloat()
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
