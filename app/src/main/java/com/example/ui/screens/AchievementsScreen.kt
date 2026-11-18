package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AchievementCategory
import com.example.data.model.AchievementProgress
import com.example.data.model.AchievementSummary
import com.example.ui.components.AchievementMedalEmblem
import com.example.ui.components.BadgeDetailDialog
import com.example.ui.components.badgeTierColors
import com.example.ui.components.pressScale
import com.example.ui.theme.CalorieAmber
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.LocalPulseRunColors
import com.example.ui.theme.StepBlue
import com.example.ui.theme.StravaOrange
import com.example.ui.theme.SunGold
import com.example.ui.viewmodel.PulseRunUiState
import java.util.Locale

private enum class UnlockFilter(val label: String) {
    ALL("Semua Status"),
    UNLOCKED("Terbuka"),
    LOCKED("Belum Terbuka")
}

@Composable
fun AchievementsScreen(
    uiState: PulseRunUiState,
    onNavigateForCategory: (AchievementCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    val summary = uiState.achievements
    var selectedCategory by rememberSaveable { mutableStateOf(AchievementCategory.ALL) }
    var unlockFilter by rememberSaveable { mutableStateOf(UnlockFilter.ALL) }
    var selectedBadgeId by rememberSaveable { mutableStateOf<String?>(null) }

    val filteredItems = remember(summary.items, selectedCategory, unlockFilter) {
        summary.items.filter { item ->
            val catMatch = selectedCategory == AchievementCategory.ALL ||
                item.definition.category == selectedCategory
            val statusMatch = when (unlockFilter) {
                UnlockFilter.ALL -> true
                UnlockFilter.UNLOCKED -> item.isUnlocked
                UnlockFilter.LOCKED -> !item.isUnlocked
            }
            catMatch && statusMatch
        }
    }

    val selectedBadgeProgress = remember(summary.items, selectedBadgeId) {
        selectedBadgeId?.let { id -> summary.items.find { it.definition.id == id } }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val columnCount = if (maxWidth >= 600.dp) 3 else 2
        val chunkedRows = remember(filteredItems, columnCount) {
            filteredItems.chunked(columnCount)
        }

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "achievements_header", contentType = "header") {
                Column {
                    Text(
                        text = "Pencapaian & Lencana",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "Kumpulkan medali dari streak lari, jarak tempuh, dan skor kuis",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                }
            }

            // Hero Rank & Progress Card
            item(key = "achievements_hero", contentType = "hero") {
                AchievementsHeroCard(
                    athleteName = uiState.preferences.athleteName,
                    summary = summary
                )
            }

            // Next Milestone Spotlight
            summary.nextTarget?.let { target ->
                item(key = "next_milestone_card", contentType = "spotlight") {
                    NextMilestoneCard(
                        progress = target,
                        onClick = { selectedBadgeId = target.definition.id }
                    )
                }
            }

            // Category Filter Chips
            item(key = "category_filters", contentType = "filters") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = AchievementCategory.entries,
                            key = { it.name }
                        ) { category ->
                            val isSelected = selectedCategory == category
                            val countInCat = if (category == AchievementCategory.ALL) {
                                summary.unlockedCount
                            } else {
                                summary.items.count { it.definition.category == category && it.isUnlocked }
                            }
                            val totalInCat = if (category == AchievementCategory.ALL) {
                                summary.totalCount
                            } else {
                                summary.items.count { it.definition.category == category }
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = {
                                    Text(
                                        text = "${category.label} ($countInCat/$totalInCat)",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StravaOrange.copy(alpha = 0.16f),
                                    selectedLabelColor = StravaOrange
                                ),
                                modifier = Modifier.testTag("achievement_filter_${category.name.lowercase()}")
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UnlockFilter.entries.forEach { mode ->
                            val selected = unlockFilter == mode
                            val pillBg by animateColorAsState(
                                targetValue = if (selected) EmeraldAccent.copy(alpha = 0.16f) else palette.cardBackground,
                                animationSpec = tween(200),
                                label = "unlock_pill_bg"
                            )
                            val pillBorder by animateColorAsState(
                                targetValue = if (selected) EmeraldAccent.copy(alpha = 0.5f) else palette.cardBorder,
                                animationSpec = tween(200),
                                label = "unlock_pill_border"
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(pillBg)
                                    .border(1.dp, pillBorder, CircleShape)
                                    .clickable { unlockFilter = mode }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                                    .testTag("achievement_status_${mode.name.lowercase()}")
                            ) {
                                Text(
                                    text = mode.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) EmeraldAccent else palette.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            if (chunkedRows.isEmpty()) {
                item(key = "empty_badges", contentType = "empty") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(palette.cardBackground)
                            .border(1.dp, palette.cardBorder, RoundedCornerShape(22.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = palette.textSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Belum ada lencana di kategori ini",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Coba ubah filter status atau mulai aktivitas baru untuk membuka lencana!",
                            fontSize = 12.sp,
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(
                    items = chunkedRows,
                    key = { row -> row.joinToString("_") { it.definition.id } },
                    contentType = { "badge_row" }
                ) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { progress ->
                            BadgeGridCard(
                                progress = progress,
                                onClick = { selectedBadgeId = progress.definition.id },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(columnCount - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    selectedBadgeProgress?.let { progress ->
        BadgeDetailDialog(
            progress = progress,
            onActionClick = onNavigateForCategory,
            onDismiss = { selectedBadgeId = null }
        )
    }
}

@Composable
private fun AchievementsHeroCard(
    athleteName: String,
    summary: AchievementSummary
) {
    val ratio = if (summary.totalCount > 0) {
        summary.unlockedCount.toFloat() / summary.totalCount.toFloat()
    } else 0f
    val animatedRatio by animateFloatAsState(
        targetValue = ratio.coerceIn(0f, 1f),
        animationSpec = tween(550),
        label = "achievements_ratio"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF151C2C), Color(0xFF1E293B), Color(0xFF151C2C))
                )
            )
            .border(1.dp, Color(0xFF334155).copy(alpha = 0.6f), RoundedCornerShape(26.dp))
            .padding(20.dp)
            .testTag("achievements_hero_card")
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(SunGold.copy(alpha = 0.24f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(SunGold, StravaOrange))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = "Koleksi Lencana",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            text = summary.athleteRankTitle,
                            fontFamily = FredokaFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$athleteName • ${summary.totalBadgePoints} XP Lencana",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SunGold.copy(alpha = 0.18f))
                        .border(1.dp, SunGold.copy(alpha = 0.4f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${summary.unlockedCount}/${summary.totalCount}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SunGold,
                        style = TextStyle(fontFeatureSettings = "tnum"),
                        modifier = Modifier.testTag("achievements_unlocked_count")
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { animatedRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(CircleShape),
                    color = SunGold,
                    trackColor = Color.White.copy(alpha = 0.12f),
                    strokeCap = StrokeCap.Round
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Koleksi Medali Terbuka",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "${(ratio * 100).toInt()}% selesai",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SunGold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeroStatMini(
                    label = "STREAK LARI",
                    value = "${summary.runningStreakDays} hr",
                    accent = StravaOrange,
                    modifier = Modifier.weight(1f)
                )
                HeroStatMini(
                    label = "TOTAL JARAK",
                    value = String.format(Locale.US, "%.1f km", summary.totalRunDistanceKm),
                    accent = StepBlue,
                    modifier = Modifier.weight(1f)
                )
                HeroStatMini(
                    label = "KUIS MAKS",
                    value = "${summary.bestQuizPercentage}%",
                    accent = EmeraldAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HeroStatMini(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accent,
            style = TextStyle(fontFeatureSettings = "tnum")
        )
    }
}

@Composable
private fun NextMilestoneCard(
    progress: AchievementProgress,
    onClick: () -> Unit
) {
    val palette = LocalPulseRunColors.current
    val def = progress.definition
    val tierColors = badgeTierColors(def.tier)
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
            .clip(RoundedCornerShape(20.dp))
            .background(palette.cardBackground)
            .border(1.dp, tierColors.primary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(14.dp)
            .testTag("next_milestone_card"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AchievementMedalEmblem(
            emoji = def.emoji,
            tier = def.tier,
            unlocked = false,
            size = 54.dp
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = StravaOrange,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "TARGET TERDEKAT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = StravaOrange,
                    letterSpacing = 0.6.sp
                )
            }
            Text(
                text = def.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LinearProgressIndicator(
                progress = { progress.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = tierColors.primary,
                trackColor = palette.subtleSurface,
                strokeCap = StrokeCap.Round
            )
            Text(
                text = "${progress.progressText} • +${def.tier.points} XP",
                fontSize = 11.sp,
                color = palette.textSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = "Lihat detail target",
            tint = palette.textSecondary
        )
    }
}

@Composable
private fun BadgeGridCard(
    progress: AchievementProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPulseRunColors.current
    val def = progress.definition
    val tierColors = badgeTierColors(def.tier)
    val interactionSource = remember { MutableInteractionSource() }
    val animatedProgress by animateFloatAsState(
        targetValue = progress.progressFraction,
        animationSpec = tween(420),
        label = "badge_card_prog"
    )

    val borderColor = if (progress.isUnlocked) {
        tierColors.primary.copy(alpha = 0.45f)
    } else {
        palette.cardBorder
    }

    Column(
        modifier = modifier
            .pressScale(interactionSource)
            .clip(RoundedCornerShape(22.dp))
            .background(palette.cardBackground)
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(14.dp)
            .testTag("badge_card_${def.id}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (progress.isUnlocked) tierColors.containerTint.copy(alpha = 0.16f)
                        else palette.subtleSurface
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = def.tier.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (progress.isUnlocked) tierColors.primary else palette.textSecondary
                )
            }

            Text(
                text = "+${def.tier.points} XP",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (progress.isUnlocked) EmeraldAccent else palette.textSecondary
            )
        }

        AchievementMedalEmblem(
            emoji = def.emoji,
            tier = def.tier,
            unlocked = progress.isUnlocked,
            size = 64.dp
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = def.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                minLines = 2
            )
            Text(
                text = def.description,
                fontSize = 11.sp,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp,
                minLines = 2
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (progress.isUnlocked) EmeraldAccent else tierColors.primary,
                trackColor = palette.subtleSurface,
                strokeCap = StrokeCap.Round
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (progress.isUnlocked) "Terbuka" else progress.progressText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (progress.isUnlocked) EmeraldAccent else palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (def.category == AchievementCategory.STREAK && progress.isUnlocked) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = CalorieAmber,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
