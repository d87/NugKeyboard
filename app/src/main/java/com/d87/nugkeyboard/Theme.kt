package com.d87.nugkeyboard
import android.app.Activity
import android.os.Build
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import kotlin.math.max
import kotlin.math.min

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)



fun noncompIsSystemInDarkTheme(context: Context): Boolean {
    val configuration = context.resources.configuration
    return (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration
        .UI_MODE_NIGHT_YES
}

fun getKeyboardColors(context: Context, shouldUseDarkTheme: Boolean = true, isDynamicColor: Boolean = true):KeyboardThemeColors {
    val isDarkTheme = noncompIsSystemInDarkTheme(context)

    val colors = KeyboardThemeColors()
    // val colorScheme = makeColorScheme(keyboardView.context)
    // https://material-foundation.github.io/material-theme-builder/

    fun colorRes(resId: Int): Int {
        return ContextCompat.getColor(context, resId);
    }

    if (isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (shouldUseDarkTheme && isDarkTheme) {
            colors.backgroundColor = colorRes(android.R.color.system_neutral1_900,);

            colors.headKeyPrimaryTextColor = colorRes(android.R.color.system_accent2_500)
            colors.radialKeyPrimaryTextColor = colorRes(android.R.color.system_accent1_300)
            colors.radialKeySecondaryTextColor = colorRes(android.R.color.system_accent3_500)

            colors.normalButtonColor = colorRes(android.R.color.system_neutral1_800)
            colors.normalButtonColorAlt = colorRes(android.R.color.system_neutral2_700)
            colors.normalButtonColorHighlight = colorRes(android.R.color.system_neutral1_300)

            colors.trailColorHighlight = colorRes(android.R.color.system_accent3_300)

            colors.accentButtonColor = colorRes(android.R.color.system_neutral2_900)
            colors.accentButtonColorAlt = colorRes(android.R.color.system_neutral2_700)
            colors.accentButtonColorHighlight = colorRes(android.R.color.system_neutral2_500)
        } else {
            colors.backgroundColor = colorRes(android.R.color.system_neutral1_300,);

            colors.headKeyPrimaryTextColor = colorRes(android.R.color.system_accent2_400)
            colors.radialKeyPrimaryTextColor = colorRes(android.R.color.system_accent1_400)
            colors.radialKeySecondaryTextColor = colorRes(android.R.color.system_accent3_400)

            colors.normalButtonColor = colorRes(android.R.color.system_neutral1_50)
            colors.normalButtonColorAlt = colorRes(android.R.color.system_neutral1_100)
            colors.normalButtonColorHighlight = colorRes(android.R.color.system_accent2_100)

            colors.trailColorHighlight = colorRes(android.R.color.system_accent3_500)

            colors.accentButtonColor = colorRes(android.R.color.system_neutral2_50)
            colors.accentButtonColorAlt = colorRes(android.R.color.system_neutral2_100)
            colors.accentButtonColorHighlight = colorRes(android.R.color.system_accent2_100)
        }
    } else {

        val accent1:FloatArray = floatArrayOf(0f,0f,0f)
        val accent2:FloatArray = floatArrayOf(0f,0f,0f)
        val accent3:FloatArray = floatArrayOf(0f,0f,0f)
        val neutral1:FloatArray = floatArrayOf(0f,0f,0f)
        val neutral2:FloatArray = floatArrayOf(0f,0f,0f)
        fun colorShade(hsl: FloatArray, shadeId: Int): Int {
            var clampedShade = max(min(shadeId, 1000), 0)
            val lightness = 1000 - clampedShade
            hsl[2] = lightness/1000f
            return ColorUtils.HSLToColor(hsl)
        }

        if (shouldUseDarkTheme && isDarkTheme) {
            ColorUtils.colorToHSL(android.graphics.Color.parseColor("#bd7593"), accent1)
            ColorUtils.colorToHSL(android.graphics.Color.parseColor("#b97fbd"), accent3)
            ColorUtils.colorToHSL(android.graphics.Color.parseColor("#707070"), accent2)
            ColorUtils.colorToHSL(android.graphics.Color.parseColor("#eceff1"), neutral1)
            ColorUtils.colorToHSL(android.graphics.Color.parseColor("#d4d8d9"), neutral2)


            colors.backgroundColor = colorShade(neutral1, 900)

            colors.headKeyPrimaryTextColor = colorShade(accent2, 500)
            colors.radialKeyPrimaryTextColor = colorShade(accent1, 300)
            colors.radialKeySecondaryTextColor = colorShade(accent3, 500)

            colors.normalButtonColor = colorShade(neutral1, 800)
            colors.normalButtonColorAlt = colorShade(neutral2, 700)
            colors.normalButtonColorHighlight = colorShade(neutral1, 300)

            colors.trailColorHighlight = colorShade(accent3, 300)

            colors.accentButtonColor = colorShade(neutral2, 900)
            colors.accentButtonColorAlt = colorShade(neutral2, 700)
            colors.accentButtonColorHighlight = colorShade(neutral2, 500)
        }else {
            ColorUtils.colorToHSL(android.graphics.Color.parseColor("#bd7593"), accent1)
            ColorUtils.colorToHSL(android.graphics.Color.parseColor("#b97fbd"), accent3)
            ColorUtils.colorToHSL(android.graphics.Color.parseColor("#707070"), accent2)
            ColorUtils.colorToHSL(android.graphics.Color.parseColor("#eceff1"), neutral1)
            ColorUtils.colorToHSL(android.graphics.Color.parseColor("#d4d8d9"), neutral2)


            colors.backgroundColor = colorShade(neutral1, 300);

            colors.headKeyPrimaryTextColor = colorShade(accent2, 400)
            colors.radialKeyPrimaryTextColor = colorShade(accent1, 400)
            colors.radialKeySecondaryTextColor = colorShade(accent3, 400)

            colors.normalButtonColor = colorShade(neutral1, 50)
            colors.normalButtonColorAlt = colorShade(neutral1, 100)
            colors.normalButtonColorHighlight = colorShade(accent2, 100)

            colors.trailColorHighlight = colorShade(accent3, 500)

            colors.accentButtonColor = colorShade(neutral2, 50)
            colors.accentButtonColorAlt = colorShade(neutral2, 100)
            colors.accentButtonColorHighlight = colorShade(accent2, 100)
        }
    }
    /*
    colors.backgroundColor = android.R.color.background_dark


    colors.headKeyPrimaryTextColor = colorScheme.secondary.toArgb()
    colors.radialKeyPrimaryTextColor = colorScheme.primary.toArgb()
    colors.radialKeySecondaryTextColor = colorScheme.tertiary.toArgb()

    colors.normalButtonColor = colorScheme.surface.toArgb()
    colors.normalButtonColorAlt = colorScheme.surfaceVariant.toArgb()
    colors.normalButtonColorHighlight = colorScheme.surfaceTint.toArgb()

    colors.trailColorHighlight = colorScheme.secondary.toArgb()

    colors.accentButtonColor = colorScheme.surface.toArgb()
    colors.accentButtonColorAlt = colorScheme.surfaceVariant.toArgb()
    colors.accentButtonColorHighlight = colorScheme.surfaceTint.toArgb()

    val darkColors = KeyboardThemeColors()
    darkColors.backgroundColor = Color.parseColor("#000000")

    darkColors.headKeyPrimaryTextColor = Color.parseColor("#878788")
    darkColors.radialKeyPrimaryTextColor = Color.parseColor("#9e252b")
    darkColors.radialKeySecondaryTextColor = Color.parseColor("#638dfe")

    darkColors.normalButtonColor = Color.parseColor("#151515")
    darkColors.normalButtonColorAlt = Color.parseColor("#0d0d0d")
    darkColors.normalButtonColorHighlight = Color.parseColor("#d35c92")

    darkColors.trailColorHighlight = Color.parseColor("#d37cb2")

    darkColors.accentButtonColor = Color.parseColor("#272727")
    darkColors.accentButtonColorAlt = Color.parseColor("#1d1d1d")
    darkColors.accentButtonColorHighlight = Color.parseColor("#4c4c4c")


    val lightColors = KeyboardThemeColors()
    lightColors.backgroundColor = Color.parseColor("#9d9fa1")

    lightColors.headKeyPrimaryTextColor = Color.parseColor("#707070")
    lightColors.radialKeyPrimaryTextColor = Color.parseColor("#bd7593")
    lightColors.radialKeySecondaryTextColor = Color.parseColor("#b97fbd")

    lightColors.normalButtonColor = Color.parseColor("#eceff1")
    lightColors.normalButtonColorAlt = Color.parseColor("#e1e4e5")
    lightColors.normalButtonColorHighlight = Color.parseColor("#f0f0f0")

    lightColors.trailColorHighlight = Color.parseColor("#d37cb2")

    lightColors.accentButtonColor = Color.parseColor("#d4d8d9")
    lightColors.accentButtonColorAlt = Color.parseColor("#d4d8d9")
    lightColors.accentButtonColorHighlight = Color.parseColor("#f6fec8")
*/
    return colors
}

fun makeColorScheme(
    context: Context,
    darkTheme: Boolean = noncompIsSystemInDarkTheme(context),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
): ColorScheme {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            // val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }
    return colorScheme
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit
) {
  val colorScheme = when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      
      darkTheme -> darkScheme
      else -> lightScheme
  }
  val view = LocalView.current
  if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
  }

  MaterialTheme(
      colorScheme = colorScheme,
      typography = AppTypography,
      content = content
  )
}

