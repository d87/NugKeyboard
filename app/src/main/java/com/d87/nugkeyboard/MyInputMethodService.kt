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

    //private var keyboardView: KeyboardView? = null
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

        //kv.exampleString = "asd"
        /*val kv = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        keyboardView = kv
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
        if (inputConnection != null) {
            when (primaryCode) {
                /*Keyboard.KEYCODE_DELETE -> {
                    val selectedText = inputConnection.getSelectedText(0)

                    if (TextUtils.isEmpty(selectedText)) {
                        inputConnection.deleteSurroundingText(1, 0)
                    } else {
                        inputConnection.commitText("", 1)
                    }
                    caps = !caps
                    keyboard!!.isShifted = caps
                    keyboardView!!.invalidateAllKeys()
                }
                Keyboard.KEYCODE_SHIFT -> {
                    caps = !caps
                    keyboard!!.isShifted = caps
                    keyboardView!!.invalidateAllKeys()
                }*/
                Keyboard.KEYCODE_DONE -> inputConnection.sendKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_ENTER
                    )
                )
                else -> {
                    var code = primaryCode.toChar()
                    if (Character.isLetter(code) && caps) {
                        code = Character.toUpperCase(code)
                    }
                    Log.d("ONKEY", code.toString())
                    inputConnection.commitText(code.toString(), 1)
                }
            }
        }
    }


    override fun onText(charSequence: CharSequence) {

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