package com.d87.nugkeyboard

//import android.inputmethodservice;
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.util.Log
import android.view.View
import android.view.KeyEvent
import android.view.WindowManager.LayoutParams.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import com.d87.nugkeyboard.R

class MyInputMethodService : InputMethodService(), NugKeyboardView.OnKeyboardActionListener {

    private var keyboardView: NugKeyboardView? = null
    //private var keyboard: Keyboard? = null

    private var caps = false
    private var capsLocked = false

    fun showSoftKeyboard(view: View) {
        //if (view.requestFocus()) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            //imm.showInputMethodPicker()
        //}
    }

    fun showInputMethodDialog(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
    }

    fun hide(view: View) {
        //this.window.window?.setSoftInputMode(SOFT_INPUT_STATE_HIDDEN)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0);
    }

    override fun onCreateInputView(): View {
        val layout = layoutInflater.inflate(R.layout.sample_nug_keyboard_view, null) as FrameLayout
        val kv: NugKeyboardView = layout.findViewById(R.id.KeyboardView)

        val isDarkMode = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            else -> false
        }
        //Log.d("NIGHT_MODE", isDarkMode.toString())


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // if API > 29
            kv.isForceDarkAllowed = false
        }
        this.window.window?.navigationBarColor = Color.BLACK

        kv.resizeForLayout() // Can't set view dimensions during its initialization, so calling it here

        kv.onKeyboardActionListener = this

        keyboardView = kv

        val inputConnection = currentInputConnection
        if (inputConnection != null) {
            val sequenceBefore = inputConnection.getTextBeforeCursor(2, 0)
            if (sequenceBefore != null && sequenceBefore.isEmpty() && !capsLocked) {
                caps = true
                keyboardView!!.setState(KeyboardModifierState.CAPS, caps)
            }
        }

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

        if (primaryCode == KeyEvent.KEYCODE_SPACE) {
            val sequenceBefore = inputConnection.getTextBeforeCursor(1, 0)
            if ( sequenceBefore == "." && !capsLocked) {
                caps = true
                keyboardView!!.setState(KeyboardModifierState.CAPS, caps)
            }
        }

        if (primaryCode == KeyEvent.KEYCODE_DEL) {
            val sequenceBefore = inputConnection.getTextBeforeCursor(1, 0)
            sequenceBefore?.let{
                if ( it.length < 1 && !capsLocked) {
                    caps = true
                    keyboardView!!.setState(KeyboardModifierState.CAPS, caps)
                }
                if ( it == "." && !capsLocked) {
                    caps = false
                    keyboardView!!.setState(KeyboardModifierState.CAPS, caps)
                }
            }
        }

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
            ActionType.HIDE_KEYBOARD -> {
                hide(keyboardView!!)
            }
            ActionType.TOGGLE_NUMERIC -> {
                keyboardView ?: return
                keyboardView!!.activeLayout ?: return
                keyboardView!!.activeLayout!!.bindings!!.ToggleLayer("NumericLayer")
            }
            ActionType.INPUT_METHOD_DIALOG -> {
                keyboardView ?: return
                showInputMethodDialog(keyboardView!!)
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