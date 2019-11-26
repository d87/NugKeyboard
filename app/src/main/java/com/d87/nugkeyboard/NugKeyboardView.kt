package com.d87.nugkeyboard

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.MotionEvent
import android.os.Handler
import android.os.Message
import android.util.Log


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

        fun onAction(action: KeyboardAction)

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



    val stateSet = mutableSetOf<KeyboardModifierState>()
    fun setState(state: KeyboardModifierState, enable: Boolean) {
        if (enable) { stateSet.add(state) } else { stateSet.remove(state) }
        onKeyboardStateChanged()
    }
    fun getState(state: KeyboardModifierState): Boolean {
        return stateSet.contains(state)
    }
    fun onKeyboardStateChanged() {
        for (key in activeLayout!!.keys) {
            if (key.mainKey != null && key.mainKey is KeyboardStateAction) {
                val mainAction = key.mainKey as KeyboardStateAction
                mainAction.setState(stateSet)
            }
        }
        this.invalidate()
    }

    private var _swipeTrackers: ArrayList<SwipeTracker> = arrayListOf()
    private var _mediaPlayers: ArrayList<MediaPlayer> = arrayListOf()

    var activeLayout: KeyboardLayout? = null
    init {
        val kLayout = KeyboardLayout(this)

        val layoutJson = resources.openRawResource(R.raw.default_layout)
            .bufferedReader().use { it.readText() }

        kLayout.importJSON(layoutJson)
        kLayout.makeKeys()
        activeLayout = kLayout
        onKeyboardStateChanged()
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

            for (tracker in _swipeTrackers){
                if (tracker.hasTrail)
                    tracker.drawTrail(canvas, it.highlightSwipeTrailPaint)
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
        // IMPORTANT: Do not edit motionevent object itself, it can screw up with subsequent events
        var motionEventType: Int = actionMasked

        when(actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                pointerID = me.getPointerId(me.actionIndex)
                motionEventType = MotionEvent.ACTION_DOWN
            }
            MotionEvent.ACTION_POINTER_UP -> {
                pointerID = me.getPointerId(me.actionIndex)
                motionEventType = MotionEvent.ACTION_UP
            }
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                pointerID = me.getPointerId(0)
            }
            MotionEvent.ACTION_CANCEL -> {
                pointerID = me.getPointerId(0)
                motionEventType = MotionEvent.ACTION_UP
            }
        }

        return onModifiedTouchEvent(motionEventType, pointerID, me)
    }

    fun getKeyFromCoords(touchX: Float, touchY: Float): SwipeButton? {
        for (key in activeLayout!!.keys) {
            if (key.isInside(touchX, touchY))
                return key
        }
        return null
    }

    // Handler for UI-thread message queue, receives non-hardware events like key-repeats and long presses
    inner class InternalHandler : Handler() {
        override fun handleMessage(msg: Message) {
            //super.handleMessage(msg)

            when (msg.what) {
                KEY_REPEAT, KEY_REPEAT_DELAY -> {
                    val pointerID = msg.arg1 as Int
                    val swipeTracker = msg.obj
                    runPointerAction(pointerID)
                    val msg = _uiHandler.obtainMessage(KEY_REPEAT, pointerID, 0, swipeTracker)
                    _uiHandler.sendMessageDelayed(msg, KEY_REPEAT_TIMEOUT)
                }
                LONG_PRESS -> {
                    Log.d("LONG_PRESS", msg.arg1.toString())
                }
            }
        }
    }
    private var _uiHandler = InternalHandler()

    fun executeAction(action: KeyboardAction?) {
        action ?: return

        when(action.action) {
            ActionType.INPUT -> {
                action.keyCode?.let { onKeyboardActionListener!!.onKey(it) }
                action.text?.let { onKeyboardActionListener!!.onText(it) }
            }
            else -> {
                onKeyboardActionListener!!.onAction(action)
            }
        }
    }

    fun getKeyForPointer(pointerID: Int): SwipeButton? {
        val swipeTracker = _swipeTrackers[pointerID]
        var (x,y) = swipeTracker.getOrigin()
        // x -= paddingBottom
        // y -= paddingTop
        return getKeyFromCoords(x, y)
    }

    fun runPointerAction(pointerID: Int) {
        val swipeTracker = _swipeTrackers[pointerID]
        val (x,y) = swipeTracker.getOrigin()
        val key = getKeyFromCoords(x, y)
        key ?: return

        var action: KeyboardAction?
        if (swipeTracker.isSwiping()) {
            val angle = swipeTracker.getSwipeAngle()
            //Log.d("SWIPE_ANGLE", angle.toString() )
            action = key.getBindingByAngle(angle)
        } else {
            //Log.d( "KEYPRESS", pointerID.toString())
            action = key.config.onPressAction
        }
        key.highlightFadeOut()
        executeAction(action)
    }


    //private fun onModifiedTouchEvent(me: MotionEvent, possiblePoly: Boolean): Boolean {
    private fun onModifiedTouchEvent(motionEventType: Int,  pointerID: Int, me: MotionEvent): Boolean {
        val numSwipeTrackers = _swipeTrackers.size
        if (pointerID == numSwipeTrackers) {
            val newTracker = SwipeTracker()
            _swipeTrackers.add(newTracker)
            _mediaPlayers.add(MediaPlayer.create(context, R.raw.key_click3))
        }
        val swipeTracker = _swipeTrackers[pointerID]
        val mediaPlayer = _mediaPlayers[pointerID]

        when (motionEventType) {
            MotionEvent.ACTION_DOWN -> {
                swipeTracker.start(me, pointerID)
                var (x,y) = swipeTracker.getOrigin()
                val key = getKeyFromCoords(x, y)
                key?.let{
                    it.highlightFadeIn()
                    if (mediaPlayer.isPlaying)
                        mediaPlayer.stop()
                    mediaPlayer.reset()
                    mediaPlayer.start()

                    // TODO: Need to somehow split key repeat and non-KR actions.
                    //  - For example delete word on swipe left, but KR delete char on the same button

                    // TODO:
                    //  - Make another delayed event for initial KR OnPress
                    //  - It'll allow swipe tracker to decide in that time what action is being run
                    //  - Execute and then whether that action is KR enabled or not
                    //  - Still maybe have a pure KR mode. So probably a button should have KR-Mode setting
                    //  - And actions the particular behaviour
                    //  - Or Delayed-KR should just not have initial OnKeyDown action

                    if (key.config.enableKeyRepeat) {
                        // runPointerAction(pointerID)
                        val msg = _uiHandler.obtainMessage(KEY_REPEAT_DELAY, pointerID, 0, swipeTracker)
                        _uiHandler.sendMessageDelayed(msg, KEY_REPEAT_DELAY_TIMEOUT)
                    } else {
                        // Start LONG_PRESS countdown, swipeTracker here is passsed as "obj" to message
                        // to later identify this message for this pointer id
                        val msg = _uiHandler.obtainMessage(LONG_PRESS, pointerID, 0, swipeTracker)
                        _uiHandler.sendMessageDelayed(msg, LONG_PRESS_TIMEOUT)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                // TODO: iterate over all pointer indices
                swipeTracker.addMovement(me, pointerID)
                this.invalidate() // To redraw swipe tracker's trail
            }
            MotionEvent.ACTION_UP -> {
                _uiHandler.removeMessages(LONG_PRESS, swipeTracker)
                _uiHandler.removeMessages(KEY_REPEAT_DELAY, swipeTracker)
                _uiHandler.removeMessages(KEY_REPEAT, swipeTracker)
                // TODO: Skip after key-repeating
                runPointerAction(pointerID) // Executes current swipe or press action
                //val key = getKeyForPointer(pointerID)
                //key?.let{ it.highlightFadeOut() }
                swipeTracker.clear()
            }
        }

        return true
    }

    companion object {
        const val LONG_PRESS = 12
        const val KEY_REPEAT_DELAY = 13
        const val KEY_REPEAT = 14
        const val LONG_PRESS_TIMEOUT = 1500L
        const val KEY_REPEAT_DELAY_TIMEOUT = 250L
        const val KEY_REPEAT_TIMEOUT = 40L
    }

}
