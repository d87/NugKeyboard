package com.d87.nugkeyboard

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.MotionEvent
import android.view.Display
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.DisplayMetrics
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources.getDrawable


//import android.inputmethodservice.KeyboardView;

/**
 * TODO: document your custom view class.
 */
class NugKeyboardView : View {

    interface OnKeyboardActionListener {
        /**
         * Called when the user presses a key. This is sent before the [.onKey] is called.
         * For keys that repeat, this is only called once.
         * @param primaryCode the unicode of the key being pressed. If the touch is not on a valid
         * key, the value will be zero.
         */
        fun onPress(primaryCode: Int)

        /**
         * Called when the user releases a key. This is sent after the [.onKey] is called.
         * For keys that repeat, this is only called once.
         * @param primaryCode the code of the key that was released
         */
        fun onRelease(primaryCode: Int)

        /**
         * Send a key press to the listener.
         * @param primaryCode this is the key that was pressed
         * @param keyCodes the codes for all the possible alternative keys
         * with the primary code being the first. If the primary key code is
         * a single character such as an alphabet or number or symbol, the alternatives
         * will include other characters that may be on the same key or adjacent keys.
         * These codes are useful to correct for accidental presses of a key adjacent to
         * the intended key.
         */
        fun onKey(primaryCode: Int)

        /**
         * Sends a sequence of characters to the listener.
         * @param text the sequence of characters to be displayed.
         */
        fun onText(text: CharSequence)

        /**
         * Called when the user quickly moves the finger from right to left.
         */
        fun swipeLeft()

        /**
         * Called when the user quickly moves the finger from left to right.
         */
        fun swipeRight()

        /**
         * Called when the user quickly moves the finger from up to down.
         */
        fun swipeDown()

        /**
         * Called when the user quickly moves the finger from down to up.
         */
        fun swipeUp()
    }

    var onKeyboardActionListener: OnKeyboardActionListener? = null
    private var _OldPointerCount = 1
    private var _OldPointerX: Float = 0.toFloat()
    private var _OldPointerY: Float = 0.toFloat()

    // TODO: Dynamically expand array or something
    private var _swipeTrackers: ArrayList<SwipeTracker> = arrayListOf(SwipeTracker(), SwipeTracker(), SwipeTracker())

    private var _VerticalCorrection: Int = 0

    //private var _keys: ArrayList<SwipeButton> = arrayListOf()
    var activeLayout: KeyboardLayout? = null
    init {
        val kLayout = KeyboardLayout(this)

        val layoutJson = resources.openRawResource(R.raw.default_layout)
            .bufferedReader().use { it.readText() }

        kLayout.importJSON(layoutJson)
        kLayout.makeKeys()
        activeLayout = kLayout
    }

    private var _KeyBackground: Drawable? = null

    private var _exampleString: String? = null // TODO: use a default from R.string...
    private var _exampleColor: Int = Color.RED // TODO: use a default from R.color...
    private var _exampleDimension: Float = 0f // TODO: use a default from R.dimen...

    private var textPaint: TextPaint? = null
    private var textWidth: Float = 0f
    private var textHeight: Float = 0f

    var redPaint: Paint = Paint()
    val backgroundPaint = Paint()

    /**
     * The text to draw
     */
    var exampleString: String?
        get() = _exampleString
        set(value) {
            _exampleString = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * The font color
     */
    var exampleColor: Int
        get() = _exampleColor
        set(value) {
            _exampleColor = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * In the example view, this dimension is the font size.
     */
    var exampleDimension: Float
        get() = _exampleDimension
        set(value) {
            _exampleDimension = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * In the example view, this drawable is drawn above the text.
     */
    var exampleDrawable: Drawable? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.NugKeyboardView, defStyle, 0
        )


        _exampleString = a.getString(
            R.styleable.NugKeyboardView_exampleString
        )
        _exampleColor = a.getColor(
            R.styleable.NugKeyboardView_exampleColor,
            exampleColor
        )
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        _exampleDimension = a.getDimension(
            R.styleable.NugKeyboardView_exampleDimension,
            exampleDimension
        )

        if (a.hasValue(R.styleable.NugKeyboardView_exampleDrawable)) {
            exampleDrawable = a.getDrawable(
                R.styleable.NugKeyboardView_exampleDrawable
            )
            exampleDrawable?.callback = this
        }

        a.recycle()

        // Set up a default TextPaint object
        textPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.LEFT
        }

        redPaint.setColor(Color.GRAY)
        redPaint.style = Paint.Style.STROKE
        redPaint.isAntiAlias = true

        backgroundPaint.setColor(Color.BLACK)
        backgroundPaint.style = Paint.Style.FILL_AND_STROKE

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements()
    }

    private fun invalidateTextPaintAndMeasurements() {
        textPaint?.let {
            it.textSize = exampleDimension
            it.color = exampleColor
            textWidth = it.measureText(exampleString)
            textHeight = it.fontMetrics.bottom
        }
    }

    override  fun onSizeChanged(neww: Int, newh: Int, oldw: Int, oldh: Int) {
        //val paddingLeft = paddingLeft
        //val paddingTop = paddingTop
        //val paddingRight = paddingRight
        //val paddingBottom = paddingBottom

        //val contentWidth = width - paddingLeft - paddingRight
        //val contentHeight = height - paddingTop - paddingBottom

        val density = resources.displayMetrics.density
        //val dpHeight = resources.displayMetrics.heightPixels / density
        //val dpWidth = resources.displayMetrics.widthPixels / density

        //val keyWidth = (width / 4).toFloat()
        //val keyHeight = (height / 4).toFloat()

        activeLayout?.let{
            it.resize(neww.toFloat(), newh.toFloat(), density)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        val density = resources.displayMetrics.density
        val dpHeight = resources.displayMetrics.heightPixels / density
        val dpWidth = resources.displayMetrics.widthPixels / density

        val keyWidth = (contentWidth / 4).toFloat()
        val keyHeight = (contentHeight / 4).toFloat()


        //canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR)
        //canvas.drawColor(0x00000000, PorterDuff.Mode.SRC)
        canvas.drawPaint(backgroundPaint)

        /*exampleString?.let {
            // Draw the text.
            canvas.drawText(
                it,
                paddingLeft + (contentWidth - textWidth) / 2,
                paddingTop + (contentHeight + textHeight) / 2,
                textPaint as Paint
            )
        }*/

        activeLayout?.let {
            for (key: SwipeButton in it.keys) {
                key.draw(canvas)
            }
        }





        //val rect = Rect(0,0, 30, 30)

        //canvas.drawLine(0f,0f, width.toFloat(), height.toFloat(), redPaint)
        //canvas.drawLine(0.toFloat(), height.toFloat()/2, paddingRight.toFloat(), height.toFloat()/2, redPaint)



        // Draw the example drawable on top of the text.
        /*exampleDrawable?.let {
            it.setBounds(
                paddingLeft, paddingTop,
                paddingLeft + contentWidth, paddingTop + contentHeight
            )
            it.draw(canvas)
        }*/
    }

    override fun onTouchEvent(me: MotionEvent): Boolean {
        // Convert multi-pointer up/down events to single up/down events to
        // deal with the typical multi-pointer behavior of two-thumb typing
        val pointerCount = me.pointerCount
        val actionMasked = me.actionMasked
        var result = false
        val now = me.eventTime

        var pointerID = 0

        Log.d("MOTION", me.toString() )

        // If you have more than 1 active pointer, then ACTION_POINTER_DOWN/UP events are coming in
        // instead of a normal ACTION_DOWN/UP, these events carry pointerIndex inside of them
        // That's why actionMasked is used instead of normal action, otherwise ACTION_POINTER_DOWN
        // wouldn't register.
        // ACTION_DOWN/UP only comes for the first/last pointer remaining, so their's pointerIndex is always 0
        // ACTION_MOVE events at all times carry positions of all active pointers,
        // without specifying which one changed
        when(actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                //Log.d("NEW POINTER", me.toString())
                pointerID = me.getPointerId(me.actionIndex)
                me.action = MotionEvent.ACTION_DOWN
            }
            MotionEvent.ACTION_POINTER_UP -> {
                //Log.d("LOST POINTER", me.toString())
                pointerID = me.getPointerId(me.actionIndex)
                me.action = MotionEvent.ACTION_UP
            }
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                pointerID = me.getPointerId(0)
            }
        }

        return onModifiedTouchEvent(me, pointerID)

        /*
        if (pointerCount != _OldPointerCount) {
            if (pointerCount == 1) {
                // Send a down event for the latest pointer
                val down = MotionEvent.obtain(
                    now, now, MotionEvent.ACTION_DOWN,
                    me.x, me.y, me.metaState
                )
                result = onModifiedTouchEvent(down, false)
                down.recycle()
                // If it's an up action, then deliver the up as well.
                if (action == MotionEvent.ACTION_UP) {
                    result = onModifiedTouchEvent(me, true)
                }
            } else {
                // Send an up event for the last pointer
                val up = MotionEvent.obtain(
                    now, now, MotionEvent.ACTION_UP,
                    _OldPointerX, _OldPointerY, me.metaState
                )
                result = onModifiedTouchEvent(up, true)
                up.recycle()
            }
        } else {
            if (pointerCount == 1) {
                result = onModifiedTouchEvent(me, false)
                _OldPointerX = me.x
                _OldPointerY = me.y
            } else {
                // Don't do anything when 2 pointers are down and moving.
                result = true
            }
        }
        _OldPointerCount = pointerCount
        */
        //return result
    }

    fun getKeyFromCoords(touchX: Float, touchY: Float): SwipeButton? {
        for (key in activeLayout!!.keys) {
            if (key.isInside(touchX, touchY))
                return key
        }
        return null
    }

    class InternalHandler : Handler() {
        override fun handleMessage(msg: Message) {
            //super.handleMessage(msg)
            Log.d("MESSAGE", msg.toString())
            when (msg.what) {
                LONG_PRESS -> {
                    Log.d("LONG_PRESS", msg.arg1.toString())
                }
            }
        }
    }
    private var _uiHandler = InternalHandler()

    //private fun onModifiedTouchEvent(me: MotionEvent, possiblePoly: Boolean): Boolean {
    private fun onModifiedTouchEvent(me: MotionEvent, pointerID: Int): Boolean {
        // var touchX = me.x.toInt() - paddingBottom
        // var touchY = me.y.toInt() - paddingTop
        // if (touchY >= _VerticalCorrection)
        //     touchY += _VerticalCorrection
        val action = me.action
        val eventTime = me.eventTime
        //val key = getKeyFromCoords(touchX, touchY)    //--------- <----- Where keys get recognized
        //key ?: return false

        val swipeTracker = try {
             _swipeTrackers[pointerID]
        } catch ( e: ArrayIndexOutOfBoundsException) {
            val newTracker = SwipeTracker()
            _swipeTrackers.add(newTracker)
            if (_swipeTrackers.size-1 != pointerID ) throw e
            newTracker
        }

        when (me.action) {
            MotionEvent.ACTION_DOWN -> {
                swipeTracker.start(me, pointerID)

                // Start LONG_PRESS countdown, swipeTracker here is passsed as "obj" to message
                // to later identify this message for this pointer id
                val msg = _uiHandler.obtainMessage(LONG_PRESS, pointerID, 0, swipeTracker)
                _uiHandler.sendMessageDelayed(msg, 1500)

                var (x,y) = swipeTracker.getOrigin()
                // x -= paddingBottom
                // y -= paddingTop
                val key = getKeyFromCoords(x, y)
                key?.let{
                    it.highlightFadeIn()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                swipeTracker.addMovement(me, pointerID)
            }
            MotionEvent.ACTION_UP -> {
                _uiHandler.removeMessages(LONG_PRESS, swipeTracker)
                if (swipeTracker.isSwiping()) {
                    val angle = swipeTracker.getSwipeAngle()
                    Log.d("SWIPE_ANGLE", angle.toString() )
                    val (x,y) = swipeTracker.getOrigin()
                    val key = getKeyFromCoords(x, y)
                    key ?: return true

                    key!!.highlightFadeOut()

                    val action = key.getBindingByAngle(angle)
                    action?.let{
                        val c: Char = it.character ?: 'z'
                        val code: Int = c.toInt()
                        val keyCode = code
                        onKeyboardActionListener!!.onKey(keyCode)
                    }
                } else {
                    Log.d( "KEYPRESS", pointerID.toString())
                    val (x,y) = swipeTracker.getOrigin()
                    val key = getKeyFromCoords(x, y)
                    key ?: return true

                    val action = key.config.mainKey
                    action?.let {
                        val c: Char = it.character ?: 'z'
                        val code: Int = c.toInt()
                        val keyCode = code
                        onKeyboardActionListener!!.onKey(keyCode)
                    }
                }
            }
        }


        // mSwipeTracker.addMovement(me)

        // if (mGestureDetector!!.onTouchEvent(me)) {
        //     showPreview(NOT_A_KEY)
        //     mHandler!!.removeMessages(MSG_REPEAT)
        //     mHandler!!.removeMessages(MSG_LONGPRESS)
        //     return true
        // }

        return true
        //mPossiblePoly = possiblePoly
        /*
        // Track the last few movements to look for spurious swipes.
        if (action == MotionEvent.ACTION_DOWN) mSwipeTracker.clear()
        mSwipeTracker.addMovement(me)

        // Ignore all motion events until a DOWN.
        if (mAbortKey
            && action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_CANCEL
        ) {
            return true
        }
        if (mGestureDetector!!.onTouchEvent(me)) {
            showPreview(NOT_A_KEY)
            mHandler!!.removeMessages(MSG_REPEAT)
            mHandler!!.removeMessages(MSG_LONGPRESS)
            return true
        }
        // Needs to be called after the gesture detector gets a turn, as it may have
        // displayed the mini keyboard
        if (mMiniKeyboardOnScreen && action != MotionEvent.ACTION_CANCEL) {
            return true
        }
        /*
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mAbortKey = false
                mStartX = touchX
                mStartY = touchY
                mLastCodeX = touchX
                mLastCodeY = touchY
                mLastKeyTime = 0
                mCurrentKeyTime = 0
                mLastKey = NOT_A_KEY
                mCurrentKey = keyIndex
                mDownKey = keyIndex
                mDownTime = me.eventTime
                mLastMoveTime = mDownTime
                checkMultiTap(eventTime, keyIndex)
                onKeyboardActionListener!!.onPress(
                    if (keyIndex != NOT_A_KEY)
                        mKeys!![keyIndex].codes[0]
                    else
                        0
                )
                if (mCurrentKey >= 0 && mKeys!![mCurrentKey].repeatable) {
                    mRepeatKeyIndex = mCurrentKey
                    val msg = mHandler!!.obtainMessage(MSG_REPEAT)
                    mHandler!!.sendMessageDelayed(msg, REPEAT_START_DELAY.toLong())
                    repeatKey()
                    // Delivering the key could have caused an abort
                    if (mAbortKey) {
                        mRepeatKeyIndex = NOT_A_KEY
                        break
                    }
                }
                if (mCurrentKey != NOT_A_KEY) {
                    val msg = mHandler!!.obtainMessage(MSG_LONGPRESS, me)
                    mHandler!!.sendMessageDelayed(msg, LONGPRESS_TIMEOUT.toLong())
                }
                showPreview(keyIndex)
            }
            MotionEvent.ACTION_MOVE -> {
                var continueLongPress = false
                if (keyIndex != NOT_A_KEY) {
                    if (mCurrentKey == NOT_A_KEY) {
                        mCurrentKey = keyIndex
                        mCurrentKeyTime = eventTime - mDownTime
                    } else {
                        if (keyIndex == mCurrentKey) {
                            mCurrentKeyTime += eventTime - mLastMoveTime
                            continueLongPress = true
                        } else if (mRepeatKeyIndex == NOT_A_KEY) {
                            resetMultiTap()
                            mLastKey = mCurrentKey
                            mLastCodeX = mLastX
                            mLastCodeY = mLastY
                            mLastKeyTime = mCurrentKeyTime + eventTime - mLastMoveTime
                            mCurrentKey = keyIndex
                            mCurrentKeyTime = 0
                        }
                    }
                }
                if (!continueLongPress) {
                    // Cancel old longpress
                    mHandler!!.removeMessages(MSG_LONGPRESS)
                    // Start new longpress if key has changed
                    if (keyIndex != NOT_A_KEY) {
                        val msg = mHandler!!.obtainMessage(MSG_LONGPRESS, me)
                        mHandler!!.sendMessageDelayed(msg, LONGPRESS_TIMEOUT.toLong())
                    }
                }
                showPreview(mCurrentKey)
                mLastMoveTime = eventTime
            }
            MotionEvent.ACTION_UP -> {
                removeMessages()
                if (keyIndex == mCurrentKey) {
                    mCurrentKeyTime += eventTime - mLastMoveTime
                } else {
                    resetMultiTap()
                    mLastKey = mCurrentKey
                    mLastKeyTime = mCurrentKeyTime + eventTime - mLastMoveTime
                    mCurrentKey = keyIndex
                    mCurrentKeyTime = 0
                }
                if (mCurrentKeyTime < mLastKeyTime && mCurrentKeyTime < DEBOUNCE_TIME
                    && mLastKey != NOT_A_KEY
                ) {
                    mCurrentKey = mLastKey
                    touchX = mLastCodeX
                    touchY = mLastCodeY
                }
                showPreview(NOT_A_KEY)
                Arrays.fill(mKeyIndices, NOT_A_KEY)
                // If we're not on a repeating key (which sends on a DOWN event)
                if (mRepeatKeyIndex == NOT_A_KEY && !mMiniKeyboardOnScreen && !mAbortKey) {
                    detectAndSendKey(mCurrentKey, touchX, touchY, eventTime)
                }
                invalidateKey(keyIndex)
                mRepeatKeyIndex = NOT_A_KEY
            }
            MotionEvent.ACTION_CANCEL -> {
                removeMessages()
                dismissPopupKeyboard()
                mAbortKey = true
                showPreview(NOT_A_KEY)
                invalidateKey(mCurrentKey)
            }
        }
        mLastX = touchX
        mLastY = touchY
        return true
        */
    }*/

    /*
    private fun getKeyIndices(x: Int, y: Int, allKeys: IntArray?): Int {
        val keys = activeLayout!!.keys
        //var primaryIndex = NOT_A_KEY
        //var closestKey = NOT_A_KEY
        var closestKeyDist = mProximityThreshold + 1
        java.util.Arrays.fill(mDistances, Integer.MAX_VALUE)
        val nearestKeyIndices = keyboard!!.getNearestKeys(x, y)
        val keyCount = nearestKeyIndices.size
        for (i in 0 until keyCount) {
            val key = keys!![nearestKeyIndices[i]]
            var dist = 0
            val isInside = key.isInside(x, y)
            if (isInside) {
                primaryIndex = nearestKeyIndices[i]
            }
            if ((isProximityCorrectionEnabled && (dist = key.squaredDistanceFrom(
                    x,
                    y
                )) < mProximityThreshold || isInside) && key.codes[0] > 32
            ) {
                // Find insertion point
                val nCodes = key.codes.size
                if (dist < closestKeyDist) {
                    closestKeyDist = dist
                    closestKey = nearestKeyIndices[i]
                }
                if (allKeys == null) continue
                for (j in mDistances.indices) {
                    if (mDistances[j] > dist) {
                        // Make space for nCodes codes
                        System.arraycopy(
                            mDistances, j, mDistances, j + nCodes,
                            mDistances.size - j - nCodes
                        )
                        System.arraycopy(
                            allKeys, j, allKeys, j + nCodes,
                            allKeys.size - j - nCodes
                        )
                        for (c in 0 until nCodes) {
                            allKeys[j + c] = key.codes[c]
                            mDistances[j + c] = dist
                        }
                        break
                    }
                }
            }
        }
        if (primaryIndex == NOT_A_KEY) {
            primaryIndex = closestKey
        }
        return primaryIndex

     */
    }

    companion object {
        const val LONG_PRESS = 12
    }

}
