package com.d87.nugkeyboard

//import android.inputmethodservice;
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.view.View
import android.view.KeyEvent
import android.text.TextUtils
import android.util.Log
import android.widget.FrameLayout
import android.view.inputmethod.InputConnection

class MyInputMethodService : InputMethodService(), NugKeyboardView.OnKeyboardActionListener {

    private var keyboardView: NugKeyboardView? = null
    //private var keyboard: Keyboard? = null

    private var caps = false

    override fun onCreateInputView(): View {
        val layout = layoutInflater.inflate(R.layout.sample_nug_keyboard_view, null) as FrameLayout
        val kv: NugKeyboardView = layout.findViewById(R.id.KeyboardView)


        // TODO: load keyboard profile/layout here to change keyboard size?
        val keyboardLayoutAspectRatio = kv.activeLayout!!.layoutWidth/kv.activeLayout!!.layoutHeight
        val density = resources.displayMetrics.density
        val dpHeight = resources.displayMetrics.heightPixels / density
        val dpWidth = resources.displayMetrics.widthPixels / density

        val displayWidth = resources.displayMetrics.widthPixels
        val displayHeight = resources.displayMetrics.heightPixels
        var kbHeight = 0
        var kbWidth = 0
        if (keyboardLayoutAspectRatio >= 1) {
            kbWidth = displayWidth
            kbHeight = (displayWidth / keyboardLayoutAspectRatio).toInt()
        } else {
            kbWidth = (displayWidth * keyboardLayoutAspectRatio).toInt()
            kbHeight = displayWidth
        }

        kv.layoutParams.width = kbWidth
        kv.layoutParams.height = kbHeight
        kv.requestLayout()

        kv.onKeyboardActionListener = this

        keyboardView = kv

        //kv.exampleString = "asd"
        /*val kv = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView

        keyboard = Keyboard(this, R.xml.keys_layout)
        kv!!.keyboard = keyboard
        kv!!.setOnKeyboardActionListener(this)
        */
        return layout
    }

    override fun onPress(i: Int) {

    }

    override fun onRelease(i: Int) {

    }


    override fun onKey(primaryCode: Int) {
        val inputConnection = currentInputConnection
        inputConnection ?: return

        val eventDown = KeyEvent(
            KeyEvent.ACTION_DOWN,
            primaryCode
        )
        val eventUp = KeyEvent(
            KeyEvent.ACTION_UP,
            primaryCode
        )
        inputConnection.sendKeyEvent(eventDown)
        inputConnection.sendKeyEvent(eventUp)
    }

    override fun onText(charSequence: CharSequence) {
        val inputConnection = currentInputConnection
        inputConnection ?: return

        inputConnection.commitText(charSequence, 1)
    }

    override fun onAction(action: KeyboardAction) {
        when(action.action) {
            ActionType.CAPS_TOGGLE -> {
                val newCapsState = !keyboardView!!.getState(KeyboardModifierState.CAPS)
                keyboardView!!.setState(KeyboardModifierState.CAPS, newCapsState)
            }
        }
    }

    override fun swipeLeft() {

    }

    override fun swipeRight() {

    }

    override fun swipeDown() {

    }

    override fun swipeUp() {

    }
}