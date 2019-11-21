package com.d87.nugkeyboard

import android.graphics.drawable.Drawable

enum class ActionType {
    NOOP,
    INPUT,
    CAPS_UP,
    CAPS_DOWN,
    DELETE_CHAR,
    DELETE_WORD,
    CYCLE_LAYOUT;
    companion object {
        val actionStringToId: Map<String, ActionType> = mapOf(
            Pair("NOOP", ActionType.NOOP),
            Pair("INPUT", ActionType.INPUT),
            Pair("CAPS_UP", ActionType.CAPS_UP),
            Pair("CAPS_DOWN", ActionType.CAPS_DOWN),
            Pair("DELETE_CHAR", ActionType.DELETE_CHAR),
            Pair("DELETE_WORD", ActionType.DELETE_WORD),
            Pair("CYCLE_LAYOUT", ActionType.CYCLE_LAYOUT)
        )
        fun getByName(actionStr: String): ActionType? {
            return actionStringToId[actionStr]
        }
    }
}

enum class KeyboardModifierState {
    CAPS,
    NUMPAD,
    CUSTOM1
}

open class KeyboardAction(action: ActionType) {
    var action: ActionType = action
    //var shit: ArrayList<Int> = arrayListOf()
    var keyCode: Int = 0
    var label: String? = null
    var icon: Drawable? = null
    var scale: Float = 1f
    var isHidden = false
    var xOffset = 0f
    var yOffset = 0f
}

class KeyboardStateAction(): KeyboardAction(ActionType.NOOP) {

    var defaultAction: KeyboardAction? = null
    val stateSetList: ArrayList<Pair<Set<KeyboardModifierState>, KeyboardAction>> = arrayListOf()
    private var currentStateSet: MutableSet<KeyboardModifierState> = mutableSetOf()

    fun setState(state: KeyboardModifierState, enable: Boolean ) {
        if ( enable ) {
            currentStateSet.add(state)
        } else {
            currentStateSet.remove(state)
        }
        onStateChanged()
    }

    fun addState(stateSet: Set<KeyboardModifierState>, action: KeyboardAction) {
        val newPair = Pair(stateSet, action)
        stateSetList.add(newPair)
    }

    fun switchToAction(newAction: KeyboardAction?) {
        if (newAction != null) {
            this.action = newAction.action
            this.keyCode = newAction.keyCode
            this.label = newAction.label
            this.icon = newAction.icon
            this.scale = newAction.scale
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