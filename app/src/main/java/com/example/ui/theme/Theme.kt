package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class ViceNightExtras(
    val neonAccent: Color,
    val neonContainer: Color,
    val onNeonContainer: Color,
    val codeBlockSurface: Color,
    val cardBorder: Color,
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val purpleAtmosphereGlow: Color,
    val neonAtmosphereGlow: Color,
    val isDark: Boolean
)

private val ViceNightDarkExtras = ViceNightExtras(
    neonAccent = ViceNeonGreen,
    neonContainer = ViceNeonGreenContainerDark,
    onNeonContainer = ViceOnNeonGreenContainerDark,
    codeBlockSurface = ViceCodeBlockDark,
    cardBorder = ViceOutlineVariantDark,
    backgroundTop = ViceDeepPurpleBg,
    backgroundBottom = ViceDeepPurpleBgEnd,
    purpleAtmosphereGlow = VicePurpleGlow,
    neonAtmosphereGlow = ViceNeonGlow,
    isDark = true
)

private val ViceNightLightExtras = ViceNightExtras(
    neonAccent = ViceSecondaryLight,
    neonContainer = ViceSecondaryContainerLight,
    onNeonContainer = ViceOnSecondaryContainerLight,
    codeBlockSurface = ViceCodeBlockLight,
    cardBorder = ViceOutlineVariantLight,
    backgroundTop = ViceBackgroundLight,
    backgroundBottom = ViceBackgroundLightEnd,
    purpleAtmosphereGlow = Color.Transparent,
    neonAtmosphereGlow = Color.Transparent,
    isDark = false
)

val LocalViceNightExtras = staticCompositionLocalOf { ViceNightDarkExtras }

val ViceNightDarkColorScheme = darkColorScheme(
    primary = VicePrimaryDark,
    onPrimary = ViceOnPrimaryDark,
    primaryContainer = VicePrimaryContainerDark,
    onPrimaryContainer = ViceOnPrimaryContainerDark,
    secondary = ViceNeonGreen,
    onSecondary = ViceOnNeonGreen,
    secondaryContainer = ViceNeonGreenContainerDark,
    onSecondaryContainer = ViceOnNeonGreenContainerDark,
    tertiary = ViceTertiaryDark,
    onTertiary = ViceOnPrimaryDark,
    background = ViceDeepPurpleBg,
    onBackground = ViceOnBackgroundDark,
    surface = ViceSurfaceDark,
    onSurface = ViceOnBackgroundDark,
    surfaceVariant = ViceSurfaceVariantDark,
    onSurfaceVariant = ViceOnSurfaceVariantDark,
    surfaceContainerLowest = ViceDeepPurpleBgEnd,
    surfaceContainerLow = ViceSurfaceDark,
    surfaceContainer = ViceSurfaceVariantDark,
    surfaceContainerHigh = ViceSurfaceElevatedDark,
    surfaceContainerHighest = ViceSurfaceHighestDark,
    outline = ViceOutlineDark,
    outlineVariant = ViceOutlineVariantDark,
    error = ViceErrorDark,
    onError = ViceOnErrorDark,
    inverseSurface = ViceSurfaceLight,
    inverseOnSurface = ViceOnBackgroundLight,
    inversePrimary = VicePrimaryLight
)

val ViceNightLightColorScheme = lightColorScheme(
    primary = VicePrimaryLight,
    onPrimary = ViceOnPrimaryLight,
    primaryContainer = VicePrimaryContainerLight,
    onPrimaryContainer = ViceOnPrimaryContainerLight,
    secondary = ViceSecondaryLight,
    onSecondary = ViceOnSecondaryLight,
    secondaryContainer = ViceSecondaryContainerLight,
    onSecondaryContainer = ViceOnSecondaryContainerLight,
    tertiary = ViceTertiaryLight,
    onTertiary = ViceOnPrimaryLight,
    background = ViceBackgroundLight,
    onBackground = ViceOnBackgroundLight,
    surface = ViceSurfaceLight,
    onSurface = ViceOnBackgroundLight,
    surfaceVariant = ViceSurfaceVariantLight,
    onSurfaceVariant = ViceOnSurfaceVariantLight,
    surfaceContainerLowest = ViceSurfaceLight,
    surfaceContainerLow = ViceBackgroundLight,
    surfaceContainer = ViceSurfaceElevatedLight,
    surfaceContainerHigh = ViceSurfaceVariantLight,
    surfaceContainerHighest = ViceSurfaceHighestLight,
    outline = ViceOutlineLight,
    outlineVariant = ViceOutlineVariantLight,
    error = ViceErrorLight,
    onError = ViceOnErrorLight,
    inverseSurface = ViceSurfaceDark,
    inverseOnSurface = ViceOnBackgroundDark,
    inversePrimary = VicePrimaryDark
)

val ViceShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Draws the signature Vice Night deep purple atmospheric canvas with subtle radial purple and
 * neon-green horizon accents using the active theme tokens.
 */
fun Modifier.viceNightBackdrop(extras: ViceNightExtras): Modifier = this.drawWithCache {
    val bgBrush = Brush.verticalGradient(
        colors = listOf(extras.backgroundTop, extras.backgroundBottom)
    )
    val purpleCenter = Offset(size.width * 0.15f, size.height * 0.08f)
    val purpleRadius = (size.maxDimension * 0.55f).coerceAtLeast(1f)
    val purpleBrush = if (extras.isDark) {
        Brush.radialGradient(
            colors = listOf(extras.purpleAtmosphereGlow, Color.Transparent),
            center = purpleCenter,
            radius = purpleRadius
        )
    } else null

    val neonCenter = Offset(size.width * 0.92f, size.height * 0.22f)
    val neonRadius = (size.maxDimension * 0.42f).coerceAtLeast(1f)
    val neonBrush = if (extras.isDark) {
        Brush.radialGradient(
            colors = listOf(extras.neonAtmosphereGlow, Color.Transparent),
            center = neonCenter,
            radius = neonRadius
        )
    } else null

    onDrawBehind {
        drawRect(brush = bgBrush)
        if (purpleBrush != null) {
            drawCircle(
                brush = purpleBrush,
                radius = purpleRadius,
                center = purpleCenter
            )
        }
        if (neonBrush != null) {
            drawCircle(
                brush = neonBrush,
                radius = neonRadius,
                center = neonCenter
            )
        }
    }
}

@Composable
fun GtaCodeMasterTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ViceNightDarkColorScheme else ViceNightLightColorScheme
    val extras = if (darkTheme) ViceNightDarkExtras else ViceNightLightExtras

    CompositionLocalProvider(LocalViceNightExtras provides extras) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = ViceShapes,
            content = content
        )
    }
}
