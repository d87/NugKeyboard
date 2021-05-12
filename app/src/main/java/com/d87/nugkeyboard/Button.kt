package com.d87.nugkeyboard

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

open class SwipeButton(layout: KeyboardLayout, config: ButtonConfig) {
    val layout = layout
    var config: ButtonConfig = config

    val EDGE_LEFT = 0x01
    val EDGE_RIGHT = 0x02
    val EDGE_TOP = 0x04
    val EDGE_BOTTOM = 0x08
    val KEYCODE_SHIFT = -1
    val KEYCODE_MODE_CHANGE = -2
    val KEYCODE_CANCEL = -3
    val KEYCODE_DONE = -4
    val KEYCODE_DELETE = -5
    val KEYCODE_ALT = -6

    var edgeFlags: Int = 0
    var width: Float = 0f
    var height: Float = 0f
    var x: Float = 0f
    var y: Float = 0f
    var centerX: Float = 0f
    var centerY: Float = 0f
    var keyBackground: Drawable? = null

    val bindings: BindingsConfig?
        get() {
            return layout.bindings!!.getActiveBindingsForButton(config.id)
        }

    fun getSwipeZones(): ArrayList<BindingsConfig.SwipeZone> {
        return bindings!!.swipeZones
    }

    fun getMainAction(): KeyboardAction? {
        return layout.bindings!!.getMainAction(config.id)
    }

    fun getLongPressAction(): KeyboardAction? {
        val binds =  layout.bindings!!.getActiveBindingsForButton(config.id)
        binds ?: return null
        val action = binds.getLongPressAction()
        if (action.type != ActionType.NOOP)
            return action
        return null
    }

    fun getActionByAngle(angle: Float): KeyboardAction? {
        return layout.bindings!!.getActionByAngle(config.id, angle)
    }

    fun setState(stateSet: Set<KeyboardModifierState>) {
        // val bindingConfig = bindings
        // bindingConfig ?: return


        for (layer in layout.bindings!!.layers) {
            val bindingConfig = layer.binds[config.id]
            bindingConfig ?: continue
            val mainKey = bindingConfig.getMainAction()
            val swipeZones = bindingConfig.swipeZones!!

            if (mainKey != null && mainKey is KeyboardStateAction) {
                val mainAction = mainKey as KeyboardStateAction
                mainAction.setState(stateSet)
            }
            for (zone in swipeZones) {
                if (zone.action != null && zone.action is KeyboardStateAction) {
                    val action = zone.action as KeyboardStateAction
                    action.setState(stateSet)
                }
            }
        }
    }

    protected var highlightAlpha = 0

    val highlightFadeOutAnimation = ValueAnimator.ofInt(150, 0).apply {
        duration = 200
        addUpdateListener { updatedAnimation ->
            // You can use the animated value in a property that uses the
            // same type as the animation. In this case, you can use the
            // float value in the translationX property.
            highlightAlpha = updatedAnimation.animatedValue as Int
            layout.keyboardView.invalidate()
        }
        /*addListener(obj : Animator.AnimatorListener! {
            override fun onAnimationEnd(animation: Animator) {

            }
        })*/
    }

    fun highlightFadeIn(){
        if (highlightFadeOutAnimation.isStarted)
            highlightFadeOutAnimation.cancel() // end()?
        highlightAlpha = 150
        layout.keyboardView.invalidate()
    }
    fun highlightFadeOut(){
        highlightFadeOutAnimation.start()
    }

    open fun onResize() {
        this.centerX = x + width/2
        this.centerY = y + height/2
    }

    var normalColor: Paint = layout.theme.normalButtonPaint
    var highlightColor: Paint = layout.theme.highlightPaint

    fun applyTheme(){
        val isAccented = if (config.isAccented) 1 else 0
        val isAltColor = if (config.isAccented) 2 else 0
        val c = isAccented + isAltColor
        normalColor = when (c) {
            1 -> layout.theme.accentButtonPaint
            3 -> layout.theme.accentAltButtonPaint
            2 -> layout.theme.normalAltButtonPaint
            else -> layout.theme.normalButtonPaint
        }
    }
    init {
        applyTheme()
    }

    open fun draw(canvas: Canvas) {
        //val textWidth = primaryTextPaint.measureText(text)
        val primaryTextPaint = layout.theme.primaryTextPaint
        val secondaryTextPaint = layout.theme.secondaryTextPaint
        val tertiaryTextPaint = layout.theme.tertiaryTextPaint
        val highlightPaint = layout.theme.highlightPaint
        val density = layout.keyboardView.resources.displayMetrics.density
        val padding = (1*density).toInt()
        val roundingRadius = 7*density

        val left = x+padding
        val top = y+padding
        val right = x+width-padding
        val bottom = y+height-padding


        canvas.drawRoundRect(left, top, right, bottom, roundingRadius, roundingRadius, normalColor)
        //canvas.drawRect(x+padding, y+padding, x+width-padding, y+height-padding, normalButtonPaint)

        val textHeight = primaryTextPaint.ascent() + primaryTextPaint.descent()

        //canvas.drawCircle(centerX, centerY, 21*density, centerCirclePaint)
        //canvas.drawCircle(centerX, centerY, 19*density, normalButtonPaint)
        val bindingConfig = layout.bindings!!.getActiveBindingsForButton(config.id)!!
        val mainKey = this.getMainAction()!!
        if (!mainKey.isHidden) {
            if (mainKey.icon != null) {
                val icon = mainKey.icon!!
                val scale = mainKey.scale
                var w = icon.intrinsicWidth * scale
                var h = icon.intrinsicHeight * scale
                val left = (centerX - w / 2).toInt()
                val top = (centerY - h / 2).toInt()
                val right = (centerX + w / 2).toInt()
                val bottom = (centerY + h / 2).toInt()
                icon.setBounds(left, top, right, bottom)
                icon.draw(canvas)
            } else {
                canvas.drawText(mainKey.text ?: "", centerX, centerY - textHeight / 2, primaryTextPaint)
            }
        }

        for (zone in bindingConfig.swipeZones) {
            val midAngle = ((zone.start + zone.end)/2+bindingConfig.roll) / (180/PI)
            val distanceOriginal = 70*density
            var distance = distanceOriginal
            var dx = (sin(midAngle)*distance).toFloat()
            var dy = (-cos(midAngle)*distance).toFloat()

            val edgeMargin = 15*density

            // Clamping radial key text to stay inside rectangular button area
            // while still freely turning
            if (abs(dx) > width/2 - edgeMargin) {
                val ratio = (width/2 - edgeMargin)/abs(dx)
                distance = distanceOriginal*ratio

                dx = (sin(midAngle)*distance).toFloat()
                dy = (-cos(midAngle)*distance).toFloat()
            }
            if (abs(dy) > height/2 - edgeMargin) {
                val ratio = (height/2 - edgeMargin)/abs(dy)
                distance = distanceOriginal*ratio

                dx = (sin(midAngle)*distance).toFloat()
                dy = (-cos(midAngle)*distance).toFloat()
            }


            val paint = if (zone.action.altColor == true) { tertiaryTextPaint } else { secondaryTextPaint }
            val textHeight = paint.ascent() + paint.descent()
            canvas.drawText(zone.action.text ?: "", centerX+dx, centerY+dy-textHeight/2, paint)

           /* if (90 > zone.start && 90 < zone.end && config.y == 0f && config.x == 0f ) {
                canvas.save()

                val clipPath = Path()
                val extra = width

                val zoneStartRad = zone.start / (180/PI)
                val zoneEndRad = zone.end / (180/PI)
                var sx = (sin(zoneStartRad)*extra*5).toFloat()
                var sy = (-cos(zoneStartRad)*extra*5).toFloat()
                var ex = (sin(zoneEndRad)*extra*5).toFloat()
                var ey = (-cos(zoneEndRad)*extra*5).toFloat()

                clipPath.moveTo(centerX, centerY)
                clipPath.lineTo(centerX+sx, centerY+sy)
                clipPath.lineTo(centerX+ex, centerY+ey)
                clipPath.lineTo(centerX, centerY)

                canvas.clipPath(clipPath)

                highlightPaint.alpha = highlightAlpha
                canvas.drawRoundRect(left, top, right, bottom, roundingRadius, roundingRadius, highlightPaint)

                canvas.restore()
            }*/
        }

        if (highlightAlpha > 0) {
            highlightPaint.alpha = highlightAlpha
            canvas.drawRoundRect(left, top, right, bottom, roundingRadius, roundingRadius, highlightPaint)
        }

        //canvas.drawRect(x, y, x+width, y+height, borderPaint)
    }

    /**
     * Detects if a point falls inside this key.
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @return whether or not the point falls inside the key. If the key is attached to an edge,
     * it will assume that all points between the key and the edge are considered to be inside
     * the key.
     */
    open fun isInside(tx: Float, ty: Float): Boolean {
        return false
    }

    /**
     * Returns the square of the distance between the center of the key and the given point.
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @return the square of the distance of the point from the center of the key
     */
    fun squaredDistanceFrom(x: Float, y: Float): Float {
        val xDist = this.x + width / 2 - x
        val yDist = this.y + height / 2 - y
        return xDist * xDist + yDist * yDist
    }

    /*constructor(x: Float, y: Float) {
        this.x = x
        this.y = y


        repeatable = a.getBoolean(
            com.android.internal.R.styleable.Keyboard_Key_isRepeatable, false
        )

        modifier = a.getBoolean(
            com.android.internal.R.styleable.Keyboard_Key_isModifier, false
        )

        sticky = a.getBoolean(
            com.android.internal.R.styleable.Keyboard_Key_isSticky, false
        )

        edgeFlags = a.getInt(com.android.internal.R.styleable.Keyboard_Key_keyEdgeFlags, 0)
        edgeFlags = edgeFlags or parent.rowEdgeFlags

        icon = a.getDrawable(
            com.android.internal.R.styleable.Keyboard_Key_keyIcon
        )

        if (icon != null) {
            icon!!.setBounds(0, 0, icon!!.intrinsicWidth, icon!!.intrinsicHeight)
        }


        //label = a.getText(com.android.internal.R.styleable.Keyboard_Key_keyLabel)
        //text = a.getText(com.android.internal.R.styleable.Keyboard_Key_keyOutputText)
        label = "TestKey"
    } */

    companion object {
        private val KEY_STATE_NORMAL = intArrayOf()
        private val KEY_STATE_PRESSED = intArrayOf(android.R.attr.state_pressed)
    }
}

/*
class RoundSwipeButton(layout: KeyboardLayout, config: ButtonConfig) : SwipeButton(layout, config) {

    override fun onResize() {
        if (height != width) {
            width = height
        }
        super.onResize()
    }

    override fun isInside(tx: Float, ty: Float): Boolean {
        val radius = centerX - x
        val dx = tx - centerX
        val dy = ty - centerY
        return (dx*dx + dy*dy < radius*radius)
    }

    override fun draw(canvas: Canvas) {
        val primaryTextPaint = layout.primaryTextPaint
        val secondaryTextPaint = layout.secondaryTextPaint
        val highlightPaint = layout.highlightPaint

        //val textWidth = primaryTextPaint.measureText(text)
        val textHeight = primaryTextPaint.ascent() + primaryTextPaint.descent()

        if (mainKey.icon != null) {
            val icon = mainKey.icon!!
            var w = icon.intrinsicWidth
            var h = icon.intrinsicHeight
            val left = (centerX-w/2).toInt()
            val top = (centerY-h/2).toInt()
            val right = (centerX+w/2).toInt()
            val bottom = (centerY+h/2).toInt()
            icon.setBounds(left, top, right, bottom)
            icon.draw(canvas)
        } else {
            canvas.drawText(mainKey.character.toString(), centerX, centerY - textHeight / 2, primaryTextPaint)
        }

        for (zone in swipeZones) {
            val midAngle = (zone.start + zone.end)/2 / (180/PI)
            val distance = width * 0.3
            val dx = (sin(midAngle)*distance).toFloat()
            val dy = (-cos(midAngle)*distance).toFloat()
            val textHeight = secondaryTextPaint.ascent() + secondaryTextPaint.descent()
            canvas.drawText(zone.binding.character.toString(), centerX+dx, centerY+dy-textHeight/2, secondaryTextPaint)
        }

        //if (highlightAlpha > 0) {
        //    highlightPaint.alpha = highlightAlpha
        //    canvas.drawRect(x, y, x + width, y + height, highlightPaint)
        //}
    }
}
*/

class RectSwipeButton(layout: KeyboardLayout, config: ButtonConfig) : SwipeButton(layout, config) {
    override fun isInside(tx: Float, ty: Float): Boolean {
        return (
            (tx >= x)
            && (tx < x + width)
            && (ty >= y)
            && (ty < y + height)
        )
    }
}