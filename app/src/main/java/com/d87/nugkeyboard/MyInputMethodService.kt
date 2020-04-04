package com.d87.nugkeyboard

//import android.inputmethodservice;
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.KeyEvent
import android.widget.FrameLayout

class MyInputMethodService : InputMethodService(), NugKeyboardView.OnKeyboardActionListener {

    private var keyboardView: NugKeyboardView? = null
    //private var keyboard: Keyboard? = null

    private var caps = false
    private var capsLocked = false

    override fun onCreateInputView(): View {
        val layout = layoutInflater.inflate(R.layout.sample_nug_keyboard_view, null) as FrameLayout
        val kv: NugKeyboardView = layout.findViewById(R.id.KeyboardView)

        kv.resizeForLayout() // Can't set view dimensions during its initialization, so calling it here

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

        if (primaryCode == KeyEvent.KEYCODE_SPACE) {
            val sequenceBefore = inputConnection.getTextBeforeCursor(2, 0)
            if ( sequenceBefore == ". " && !capsLocked) {
                caps = true
                keyboardView!!.setState(KeyboardModifierState.CAPS, caps)
            }
        }
    }

    override fun onText(charSequence: CharSequence) {
        val inputConnection = currentInputConnection
        inputConnection ?: return

        inputConnection.commitText(charSequence, 1)

        if (!capsLocked) {
            caps = false
            keyboardView!!.setState(KeyboardModifierState.CAPS, caps)
        }
    }

    override fun onAction(action: KeyboardAction) {
        when(action.type) {
            ActionType.CAPS_TOGGLE -> {
                val newCapsState = !keyboardView!!.getState(KeyboardModifierState.CAPS)
                keyboardView!!.setState(KeyboardModifierState.CAPS, newCapsState)
            }
            ActionType.CAPS_UP -> {
                if (caps == true) {
                    capsLocked = true
                } else {
                    caps = true
                    capsLocked = false
                }
                keyboardView!!.setState(KeyboardModifierState.CAPS, caps)
            }
            ActionType.CAPS_DOWN -> {
                if (capsLocked) {
                    capsLocked = false
                } else {
                    caps = false
                    capsLocked = false
                }
                keyboardView!!.setState(KeyboardModifierState.CAPS, caps)
            }
            ActionType.CYCLE_LAYOUT -> {
                keyboardView!!.cycleLayout()
            }
            else -> {

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