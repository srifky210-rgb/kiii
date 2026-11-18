package com.example.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class PulseRunExtendedColors(
    val isDark: Boolean,
    val cardBackground: Color,
    val cardBorder: Color,
    val subtleSurface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val heartTint: Color,
    val heartBg: Color,
    val heartBorder: Color,
    val stepTint: Color,
    val stepBg: Color,
    val stepBorder: Color,
    val calorieTint: Color,
    val calorieBg: Color,
    val calorieBorder: Color,
    val penjasTint: Color,
    val penjasBg: Color,
    val penjasBorder: Color,
    val stravaTint: Color,
    val emeraldTint: Color
)

private val DarkExtendedColors = PulseRunExtendedColors(
    isDark = true,
    cardBackground = DarkCard,
    cardBorder = DarkBorder,
    subtleSurface = DarkSurface,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    heartTint = HeartRose,
    heartBg = DarkCard,
    heartBorder = HeartRose.copy(alpha = 0.25f),
    stepTint = StepBlue,
    stepBg = DarkCard,
    stepBorder = StepBlue.copy(alpha = 0.25f),
    calorieTint = CalorieAmber,
    calorieBg = DarkCard,
    calorieBorder = CalorieAmber.copy(alpha = 0.25f),
    penjasTint = PenjasCyan,
    penjasBg = Color(0xFF092A2E),
    penjasBorder = PenjasTeal.copy(alpha = 0.4f),
    stravaTint = StravaOrange,
    emeraldTint = EmeraldAccent
)

private val LightExtendedColors = PulseRunExtendedColors(
    isDark = false,
    cardBackground = LightCard,
    cardBorder = LightBorder,
    subtleSurface = LightSurface,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    heartTint = HeartRose,
    heartBg = Color(0xFFFFF1F2),
    heartBorder = Color(0xFFFECDD3),
    stepTint = StepBlue,
    stepBg = Color(0xFFEFF6FF),
    stepBorder = Color(0xFFBFDBFE),
    calorieTint = CalorieAmber,
    calorieBg = Color(0xFFFFFBEB),
    calorieBorder = Color(0xFFFDE68A),
    penjasTint = PenjasTealVibrant,
    penjasBg = Color(0xFFF0FDFA),
    penjasBorder = Color(0xFF99F6E4),
    stravaTint = StravaOrange,
    emeraldTint = EmeraldAccent
)

val LocalPulseRunColors = staticCompositionLocalOf { DarkExtendedColors }

private val DarkColorScheme = darkColorScheme(
    primary = StravaOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A1B08),
    onPrimaryContainer = Color(0xFFFFDBD0),
    secondary = PenjasTealVibrant,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0A3836),
    onSecondaryContainer = Color(0xFF99F6E4),
    tertiary = EmeraldAccent,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerLow = DarkCard,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = Color(0xFF233044),
    outline = DarkBorder,
    inverseSurface = LightCard,
    inverseOnSurface = LightTextPrimary,
    error = HeartRose,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = StravaOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDE6),
    onPrimaryContainer = Color(0xFF5C1B00),
    secondary = PenjasTealVibrant,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF0F3D39),
    tertiary = EmeraldAccent,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightCard,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainerLow = LightCard,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = Color(0xFFE8EEF5),
    outline = LightBorder,
    inverseSurface = Color(0xFF0F172A),
    inverseOnSurface = Color.White,
    error = HeartRose,
    onError = Color.White
)

@Composable
fun PulseRunTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val baseExtended = if (darkTheme) DarkExtendedColors else LightExtendedColors
    val targetExtended = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        baseExtended.copy(
            cardBackground = baseScheme.surfaceContainerLow,
            cardBorder = baseScheme.outlineVariant,
            subtleSurface = baseScheme.surfaceContainer,
            textPrimary = baseScheme.onSurface,
            textSecondary = baseScheme.onSurfaceVariant,
            stravaTint = baseScheme.primary,
            penjasTint = baseScheme.secondary
        )
    } else {
        baseExtended
    }

    val animSpec = tween<Color>(durationMillis = 240)

    val background by animateColorAsState(baseScheme.background, animSpec, label = "bg")
    val surface by animateColorAsState(baseScheme.surface, animSpec, label = "surface")
    val cardBg by animateColorAsState(targetExtended.cardBackground, animSpec, label = "cardBg")
    val cardBorder by animateColorAsState(targetExtended.cardBorder, animSpec, label = "cardBorder")
    val subtleSurface by animateColorAsState(targetExtended.subtleSurface, animSpec, label = "subtle")
    val textPrimary by animateColorAsState(targetExtended.textPrimary, animSpec, label = "textPrimary")
    val textSecondary by animateColorAsState(targetExtended.textSecondary, animSpec, label = "textSecondary")
    val containerHigh by animateColorAsState(baseScheme.surfaceContainerHigh, animSpec, label = "containerHigh")

    val animatedScheme = baseScheme.copy(
        background = background,
        surface = surface,
        onBackground = textPrimary,
        onSurface = textPrimary,
        surfaceVariant = subtleSurface,
        onSurfaceVariant = textSecondary,
        surfaceContainerLow = cardBg,
        surfaceContainer = subtleSurface,
        surfaceContainerHigh = containerHigh,
        outline = cardBorder
    )
    val animatedExtended = targetExtended.copy(
        cardBackground = cardBg,
        cardBorder = cardBorder,
        subtleSurface = subtleSurface,
        textPrimary = textPrimary,
        textSecondary = textSecondary
    )

    CompositionLocalProvider(LocalPulseRunColors provides animatedExtended) {
        MaterialTheme(
            colorScheme = animatedScheme,
            typography = PulseRunTypography,
            content = content
        )
    }
}
