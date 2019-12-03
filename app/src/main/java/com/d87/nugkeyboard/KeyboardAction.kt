package com.d87.nugkeyboard

import android.graphics.drawable.Drawable
import android.view.KeyEvent

enum class ActionType {
    NOOP,
    INPUT,
    CONTINUE,
    CAPS_UP,
    CAPS_DOWN,
    CAPS_TOGGLE,
    DELETE_CHAR,
    DELETE_WORD,
    CYCLE_LAYOUT;
    companion object {
        private val map = values().associateBy({ it.name }, { it })
        fun getByName(name: String): ActionType? {
            return map[name]
        }
    }
}

enum class KeyboardModifierState {
    CAPS,
    NUMPAD,
    CUSTOM1;
    companion object {
        private val map = values().associateBy({ it.name }, { it })
        fun getByName(name: String): KeyboardModifierState? {
            return map[name]
        }
    }
}

class KeyCodes {
    companion object {
        private val map = mapOf(
            // TODO: Log error if constant not found
            Pair("KEYCODE_ENTER", KeyEvent.KEYCODE_ENTER),
            Pair("KEYCODE_DEL", KeyEvent.KEYCODE_DEL), // Backspace
            Pair("KEYCODE_BACKSPACE", KeyEvent.KEYCODE_DEL), // Backspace
            Pair("KEYCODE_FORWARD_DEL", KeyEvent.KEYCODE_FORWARD_DEL), // Delete
            Pair("KEYCODE_DELETE", KeyEvent.KEYCODE_FORWARD_DEL), // Delete
            Pair("KEYCODE_END", KeyEvent.KEYCODE_MOVE_END), // End
            Pair("KEYCODE_HOME", KeyEvent.KEYCODE_HOME), // There's also MOVE_HOME
            Pair("KEYCODE_TAB", KeyEvent.KEYCODE_TAB),

            Pair("KEYCODE_DPAD_RIGHT", KeyEvent.KEYCODE_DPAD_RIGHT),
            Pair("KEYCODE_DPAD_LEFT", KeyEvent.KEYCODE_DPAD_LEFT),

            Pair("KEYCODE_SHIFT_LEFT", KeyEvent.KEYCODE_SHIFT_LEFT),
            Pair("KEYCODE_SHIFT", KeyEvent.KEYCODE_SHIFT_LEFT),
            Pair("KEYCODE_SHIFT_RIGHT", KeyEvent.KEYCODE_SHIFT_RIGHT),

            Pair("KEYCODE_CTRL_LEFT", KeyEvent.KEYCODE_CTRL_LEFT),
            Pair("KEYCODE_CTRL", KeyEvent.KEYCODE_CTRL_LEFT),
            Pair("KEYCODE_CTRL_RIGHT", KeyEvent.KEYCODE_CTRL_RIGHT),

            Pair("KEYCODE_ALT_LEFT", KeyEvent.KEYCODE_ALT_LEFT),
            Pair("KEYCODE_ALT", KeyEvent.KEYCODE_ALT_LEFT),
            Pair("KEYCODE_ALT_RIGHT", KeyEvent.KEYCODE_ALT_RIGHT),

            Pair("KEYCODE_CUT", KeyEvent.KEYCODE_CUT),
            Pair("KEYCODE_COPY", KeyEvent.KEYCODE_COPY),
            Pair("KEYCODE_SPACE", KeyEvent.KEYCODE_SPACE)
        )
        operator fun get(name: String) = map[name]
    }
}

open class KeyboardAction(action: ActionType) {
    var action: ActionType = action
    //var shit: ArrayList<Int> = arrayListOf()
    var text: String? = null
    var keyCode: Int? = null
    var label: String? = null
    var icon: Drawable? = null
    var scale: Float = 1f
    var altColor: Boolean = false
    var isHidden = false
    var xOffset = 0f
    var yOffset = 0f
}

class KeyboardStateAction(): KeyboardAction(ActionType.NOOP) {

    var defaultAction: KeyboardAction? = null
    val stateSetList: ArrayList<Pair<Set<KeyboardModifierState>, KeyboardAction>> = arrayListOf()
    private var currentStateSet: Set<KeyboardModifierState>? = null

    fun setState(newStateSet: Set<KeyboardModifierState>) {
        currentStateSet = newStateSet
        onStateChanged()
    }

    fun addState(stateSet: Set<KeyboardModifierState>, action: KeyboardAction) {
        val newPair = Pair(stateSet, action)
        stateSetList.add(newPair)
    }

    fun switchToAction(newAction: KeyboardAction?) {
        if (newAction != null) {
            this.action = newAction.action
            this.text = newAction.text
            this.keyCode = newAction.keyCode
            this.label = newAction.label
            this.icon = newAction.icon
            this.scale = newAction.scale
            this.altColor = newAction.altColor
            this.xOffset = newAction.xOffset
            this.yOffset = newAction.yOffset
        } else {
            this.action = ActionType.NOOP
            this.icon = null
            this.label = null
            this.scale = 1f
        }
    }

    fun onStateChanged() {
        var foundAction: KeyboardAction? = null
        for ((stateSet, action) in stateSetList) {
            if (stateSet.equals(currentStateSet)) {
                foundAction = action
                break
            }
        }
        switchToAction(foundAction ?: defaultAction)
    }
}