package com.d87.nugkeyboard

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint

class KeyboardThemeColors {
    var backgroundColor = Color.BLACK

    var headKeyPrimaryTextColor = Color.BLACK
    var radialKeyPrimaryTextColor = Color.BLACK
    var radialKeySecondaryTextColor = Color.BLACK

    var normalButtonColor = Color.BLACK
    var normalButtonColorAlt = Color.BLACK
    var normalButtonColorHighlight = Color.BLACK

    var trailColorHighlight = Color.BLACK

    var accentButtonColor = Color.BLACK
    var accentButtonColorAlt = Color.BLACK
    var accentButtonColorHighlight = Color.BLACK
}

class KeyboardTheme(var colors: KeyboardThemeColors, var displayDensity: Float = 1.0f) {
    val backgroundPaint: TextPaint = TextPaint().apply {
        typeface = Typeface.DEFAULT_BOLD
        flags = Paint.ANTI_ALIAS_FLAG
        textAlign = Paint.Align.CENTER
        color = colors.backgroundColor
        textSize = 36f*displayDensity
    }

    val primaryTextPaint: TextPaint = TextPaint().apply {
        typeface = Typeface.DEFAULT_BOLD
        flags = Paint.ANTI_ALIAS_FLAG
        textAlign = Paint.Align.CENTER
        color = colors.headKeyPrimaryTextColor
        textSize = 36f*displayDensity
    }
    val secondaryTextPaint: TextPaint = TextPaint().apply {
        typeface = Typeface.DEFAULT_BOLD
        flags = Paint.ANTI_ALIAS_FLAG
        textAlign = Paint.Align.CENTER
        color = colors.radialKeyPrimaryTextColor
        textSize = 24f*displayDensity
    }
    val tertiaryTextPaint: TextPaint = TextPaint().apply {
        typeface = Typeface.DEFAULT_BOLD
        flags = Paint.ANTI_ALIAS_FLAG
        textAlign = Paint.Align.CENTER
        color = colors.radialKeySecondaryTextColor
        textSize = 24f*displayDensity
    }
    val highlightPaint: Paint = Paint().apply{
        style = Paint.Style.FILL
        color = colors.normalButtonColorHighlight
    }
    val highlightSwipeTrailPaint: Paint = Paint().apply{
        style = Paint.Style.STROKE
        strokeWidth = 15f
        color = colors.trailColorHighlight
    }
    val normalButtonPaint: Paint = Paint().apply{
        style = Paint.Style.FILL
        color = colors.normalButtonColor
    }
    val accentButtonPaint: Paint = Paint().apply{
        style = Paint.Style.FILL
        color = colors.accentButtonColor
    }
    val normalAltButtonPaint: Paint = Paint().apply{
        style = Paint.Style.FILL
        color = colors.normalButtonColorAlt
    }
    val accentAltButtonPaint: Paint = Paint().apply{
        style = Paint.Style.FILL
        color = colors.accentButtonColorAlt
    }
}