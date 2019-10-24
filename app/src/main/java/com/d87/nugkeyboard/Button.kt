package com.d87.nugkeyboard

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.widget.Button
import androidx.core.animation.addListener

open class SwipeButton(config: ButtonConfig, keyboardView: NugKeyboardView) { //(parent: Row) {

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

    var code = "K"

    /** Label to display  */
    var label: CharSequence = "Key"

    /**
     * Flags that specify the anchoring to edges of the keyboard for detecting touch events
     * that are just out of the boundary of the key. This is a bit mask of
     * [Keyboard.EDGE_LEFT], [Keyboard.EDGE_RIGHT], [Keyboard.EDGE_TOP] and
     * [Keyboard.EDGE_BOTTOM].
     */
    var edgeFlags: Int = 0
    /** Width of the key, not including the gap  */
    var width: Float = 0f
    /** Height of the key, not including the gap  */
    var height: Float = 0f
    /** X coordinate of the key in the keyboard layout  */
    var x: Float = 0f
    /** Y coordinate of the key in the keyboard layout  */
    var y: Float = 0f
    var centerX: Float = 0f
    var centerY: Float = 0f

    var config: ButtonConfig = config

    var roll: Float = config.roll
    var type: String = "Normal"
    var divisions: Int = config.divisions

    /** The current pressed state of this key  */
    var pressed: Boolean = false
    /** If this is a sticky key, is it on?  */
    var on: Boolean = false
    /** Whether this key is sticky, i.e., a toggle key  */
    var sticky: Boolean = false

    val borderPaint: Paint = Paint().apply{
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
        //isAntiAlias = true
    }
    val primaryTextPaint: TextPaint = TextPaint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        textAlign = Paint.Align.CENTER
        color = Color.RED
        textSize = 60f
    }
    val secondaryTextPaint: TextPaint = TextPaint()
    val highlightPaint: Paint = Paint().apply{
        style = Paint.Style.FILL
        color = Color.CYAN
    }

    private var _keyboardView: NugKeyboardView = keyboardView
    private var _highlightAlpha = 0

    val alphaAnimation = ValueAnimator.ofInt(0, 150, 0).apply {
        duration = 300
        addUpdateListener { updatedAnimation ->
            // You can use the animated value in a property that uses the
            // same type as the animation. In this case, you can use the
            // float value in the translationX property.
            _highlightAlpha = updatedAnimation.animatedValue as Int
            _keyboardView.invalidate()
        }
        /*addListener(obj : Animator.AnimatorListener! {
            override fun onAnimationEnd(animation: Animator) {

            }
        })*/
    }

    /**
     * Informs the key that it has been pressed, in case it needs to change its appearance or
     * state.
     * @see .onReleased
     */
    fun startHIghlight(){
        alphaAnimation.start()
    }

    open fun onResize() {
        this.centerX = x + width/2
        this.centerY = y + height/2
    }

    fun onPressed() {
        pressed = !pressed
    }

    /**
     * Changes the pressed state of the key.
     *
     *
     * Toggled state of the key will be flipped when all the following conditions are
     * fulfilled:
     *
     *
     *  * This is a sticky key, that is, [.sticky] is `true`.
     *  * The parameter `inside` is `true`.
     *  * [android.os.Build.VERSION.SDK_INT] is greater than
     * [android.os.Build.VERSION_CODES.LOLLIPOP_MR1].
     *
     *
     * @param inside whether the finger was released inside the key. Works only on Android M and
     * later. See the method document for details.
     * @see .onPressed
     */
    fun onReleased(inside: Boolean) {
        pressed = !pressed
        if (sticky && inside) {
            on = !on
        }
    }

    open fun draw(canvas: Canvas) {
        val text = "A"
        val textWidth = primaryTextPaint.measureText(text)
        val textHeight = primaryTextPaint.ascent() + primaryTextPaint.descent()

        canvas.drawText(text, centerX, centerY-textHeight/2, primaryTextPaint)

        if (_highlightAlpha > 0) {
            highlightPaint.alpha = _highlightAlpha
            canvas.drawRect(x, y, x + width, y + height, highlightPaint)
        }

        canvas.drawRect(x, y, x+width, y+height, borderPaint)
    }

    /**
     * Detects if a point falls inside this key.
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @return whether or not the point falls inside the key. If the key is attached to an edge,
     * it will assume that all points between the key and the edge are considered to be inside
     * the key.
     */
    open fun isInside(tx: Int, ty: Int): Boolean {
        return false
        /*val leftEdge = edgeFlags and EDGE_LEFT > 0
        val rightEdge = edgeFlags and EDGE_RIGHT > 0
        val topEdge = edgeFlags and EDGE_TOP > 0
        val bottomEdge = edgeFlags and EDGE_BOTTOM > 0
        return (
                (tx >= x || leftEdge && tx <= x + width)
            && (tx < x + width || rightEdge && tx >= x)
            && (ty >= y || topEdge && ty <= y + height)
            && (ty < y + height || bottomEdge && ty >= y)
        )*/
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
}

class RoundSwipeButton(config: ButtonConfig, keyboardView: NugKeyboardView) : SwipeButton(config, keyboardView) {
    override fun onResize() {
        if (height != width) {
            width = height
        }
        super.onResize()
    }

    override fun isInside(tx: Int, ty: Int): Boolean {
        val radius = centerX - x
        val dx = tx - centerX
        val dy = ty - centerY
        return (dx*dx + dy*dy < radius*radius)
    }
}

class RectSwipeButton(config: ButtonConfig, keyboardView: NugKeyboardView) : SwipeButton(config, keyboardView) {
    override fun isInside(tx: Int, ty: Int): Boolean {
        return (
            (tx >= x)
            && (tx < x + width)
            && (ty >= y)
            && (ty < y + height)
        )
    }
}