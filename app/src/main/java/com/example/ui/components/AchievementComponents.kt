package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AchievementCategory
import com.example.data.model.AchievementDefinition
import com.example.data.model.AchievementProgress
import com.example.data.model.BadgeTier
import com.example.ui.theme.CalorieAmber
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LocalPulseRunColors
import com.example.ui.theme.PenjasCyan
import com.example.ui.theme.PenjasTealVibrant
import com.example.ui.theme.StepBlue
import com.example.ui.theme.StravaOrange
import com.example.ui.theme.SunGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TierVisualColors(
    val primary: Color,
    val secondary: Color,
    val containerTint: Color
)

fun badgeTierColors(tier: BadgeTier): TierVisualColors = when (tier) {
    BadgeTier.BRONZE -> TierVisualColors(
        primary = Color(0xFFD97706),
        secondary = Color(0xFFB45309),
        containerTint = Color(0xFFD97706)
    )
    BadgeTier.SILVER -> TierVisualColors(
        primary = StepBlue,
        secondary = Color(0xFF94A3B8),
        containerTint = StepBlue
    )
    BadgeTier.GOLD -> TierVisualColors(
        primary = SunGold,
        secondary = StravaOrange,
        containerTint = CalorieAmber
    )
    BadgeTier.PLATINUM -> TierVisualColors(
        primary = PenjasCyan,
        secondary = EmeraldAccent,
        containerTint = PenjasTealVibrant
    )
}

@Composable
fun AchievementMedalEmblem(
    emoji: String,
    tier: BadgeTier,
    unlocked: Boolean,
    size: Dp = 62.dp,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    val tierColors = badgeTierColors(tier)
    val lockedRing = if (palette.isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
    val lockedInner = if (palette.isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = this.size.minDimension / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            if (unlocked) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            tierColors.primary.copy(alpha = 0.28f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(tierColors.primary, tierColors.secondary)
                    ),
                    radius = radius * 0.86f,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.32f),
                    radius = radius * 0.74f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            } else {
                drawCircle(
                    color = lockedInner,
                    radius = radius * 0.84f,
                    center = center
                )
                drawCircle(
                    color = lockedRing,
                    radius = radius * 0.84f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        Text(
            text = emoji,
            fontSize = (size.value * 0.40f).sp,
            modifier = Modifier.graphicsLayer {
                alpha = if (unlocked) 1f else 0.38f
            }
        )

        if (!unlocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(palette.cardBackground)
                    .border(1.dp, lockedRing, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Terkunci",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(11.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(EmeraldAccent)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Terbuka",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
fun BadgeDetailDialog(
    progress: AchievementProgress,
    onActionClick: (AchievementCategory) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalPulseRunColors.current
    val def = progress.definition
    val tierColors = badgeTierColors(def.tier)
    val dateLabel = remember(progress.unlockedAtMillis) {
        progress.unlockedAtMillis?.let { millis ->
            SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.forLanguageTag("id-ID")).format(Date(millis))
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = palette.cardBackground,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (progress.isUnlocked) tierColors.primary.copy(alpha = 0.45f) else palette.cardBorder,
                    shape = RoundedCornerShape(28.dp)
                )
                .testTag("badge_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AchievementMedalEmblem(
                    emoji = def.emoji,
                    tier = def.tier,
                    unlocked = progress.isUnlocked,
                    size = 86.dp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(tierColors.containerTint.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${def.tier.label} • +${def.tier.points} XP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = tierColors.primary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(palette.subtleSurface)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = def.category.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textSecondary
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = def.title,
                        fontFamily = FredokaFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = def.description,
                        fontSize = 13.sp,
                        color = palette.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.subtleSurface)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (progress.isUnlocked) "Pencapaian Tercapai" else "Progres Saat Ini",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textSecondary
                        )
                        Text(
                            text = progress.progressText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (progress.isUnlocked) EmeraldAccent else tierColors.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = if (progress.isUnlocked) EmeraldAccent else tierColors.primary,
                        trackColor = palette.cardBackground,
                        strokeCap = StrokeCap.Round
                    )
                    if (progress.isUnlocked && dateLabel != null) {
                        Text(
                            text = "Terbuka pada $dateLabel",
                            fontSize = 11.sp,
                            color = EmeraldAccent,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (!progress.isUnlocked) {
                        Text(
                            text = "Tips: ${def.actionHint}",
                            fontSize = 11.sp,
                            color = palette.textSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("badge_detail_close")
                    ) {
                        Text("Tutup", fontWeight = FontWeight.SemiBold)
                    }

                    if (!progress.isUnlocked) {
                        val ctaText = when (def.category) {
                            AchievementCategory.QUIZ -> "Buka Kuis"
                            AchievementCategory.MILESTONE -> if (def.id == "penjas_all_8") "Latihan Penjas" else "Mulai Lari"
                            else -> "Mulai Lari"
                        }
                        Button(
                            onClick = {
                                onDismiss()
                                onActionClick(def.category)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StravaOrange,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .testTag("badge_detail_action")
                        ) {
                            Text(ctaText, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementCelebrationDialog(
    badges: List<AchievementDefinition>,
    onViewBadges: () -> Unit,
    onDismiss: () -> Unit
) {
    if (badges.isEmpty()) return
    val palette = LocalPulseRunColors.current
    var reveal by remember { mutableStateOf(false) }
    LaunchedEffect(badges) { reveal = true }
    val scale by animateFloatAsState(
        targetValue = if (reveal) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "celebration_scale"
    )
    val totalXp = badges.sumOf { it.tier.points }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = palette.cardBackground,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .border(1.5.dp, SunGold.copy(alpha = 0.55f), RoundedCornerShape(28.dp))
                .testTag("achievement_celebration_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(listOf(SunGold, StravaOrange))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = "Pencapaian Baru",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (badges.size == 1) "Lencana Baru Terbuka!" else "${badges.size} Lencana Baru Terbuka!",
                        fontFamily = FredokaFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Hebat! Kamu meraih +$totalXp XP tambahan untuk profil atletmu.",
                        fontSize = 12.sp,
                        color = EmeraldAccent,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    badges.forEach { badge ->
                        val tierColors = badgeTierColors(badge.tier)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(palette.subtleSurface)
                                .border(1.dp, tierColors.primary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AchievementMedalEmblem(
                                emoji = badge.emoji,
                                tier = badge.tier,
                                unlocked = true,
                                size = 48.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = badge.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )
                                Text(
                                    text = "${badge.tier.label} • +${badge.tier.points} XP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = tierColors.primary
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("celebration_dismiss_button")
                    ) {
                        Text("Tutup", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            onDismiss()
                            onViewBadges()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StravaOrange,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("celebration_view_badges_button")
                    ) {
                        Text("Lihat Lencana", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
