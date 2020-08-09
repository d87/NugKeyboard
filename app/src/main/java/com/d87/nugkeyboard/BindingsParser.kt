package com.d87.nugkeyboard

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class BindingsParser(val kbLayout: KeyboardLayout, val parser: XmlPullParser) {
    var layer: BindingsLayer? = null
    val ns: String? = null

    val ONPRESS = 1
    val ONSWIPE = 2

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    fun parseActionAttributes(action: KeyboardAction): KeyboardAction {
        for (i in 0 until parser.attributeCount) {
            val name = parser.getAttributeName(i)
            val value = parser.getAttributeValue(i)
            when (name) {
                "hidden" -> action.isHidden = value.toBoolean()
                "icon" -> action.icon = kbLayout.getDrawableByName(value)
                "scale" -> action.scale = value.toFloatOrNull() ?: 1f
                "altColor" -> action.altColor = value.toBoolean()
            }
        }
        return action
    }

    fun parseAction(): KeyboardAction? {
        when (parser.name) {
            "CapInput" -> {
                val stateAction = KeyboardStateAction()

                val lowerCaseAction = KeyboardAction(ActionType.INPUT)
                stateAction.defaultAction = parseActionAttributes(lowerCaseAction)

                val upperCaseAction = KeyboardAction(ActionType.INPUT)

                parser.next()
                val text = parser.text
                return if (text == null) {
                    KeyboardAction(ActionType.NOOP)
                } else {

                    lowerCaseAction.text = text.toLowerCase()
                    upperCaseAction.text = text.toUpperCase()

                    val capsStateSet = mutableSetOf<KeyboardModifierState>()
                    capsStateSet.add(KeyboardModifierState.CAPS)

                    stateAction.addState(capsStateSet, parseActionAttributes(upperCaseAction))

                    stateAction
                }
            }
            "Input" -> {
                val action = KeyboardAction(ActionType.INPUT)
                parseActionAttributes(action)

                val text = parser.nextText()
                return if (text == null) {
                    KeyboardAction(ActionType.NOOP)
                } else {
                    action.text = text
                    action
                }
            }
            "Action" -> {
                val dummyAction = KeyboardAction(ActionType.NOOP)
                parseActionAttributes(dummyAction)
                val text = parser.nextText()
                return if (text == null) {
                    KeyboardAction(ActionType.NOOP)
                } else {
                    val actionId = ActionType.getByName(text) ?: ActionType.NOOP
                    val action = KeyboardAction(actionId)
                    action.icon = dummyAction.icon
                    action.isHidden = dummyAction.isHidden
                    action.scale = dummyAction.scale
                    action.altColor = dummyAction.altColor
                    action
                }
            }
            "KeyCode" -> {
                val action = KeyboardAction(ActionType.INPUT)
                parseActionAttributes(action)

                val text = parser.nextText()
                return if (text == null) {
                    KeyboardAction(ActionType.NOOP)
                } else {
                    if (text.startsWith('@')) {
                        val keycodeName = text.substring(1)
                        action.keyCode = KeyCodes[keycodeName]
                    } else {
                        action.keyCode = text.toIntOrNull()
                    }
                    action
                }
            }
            "Symbol" -> {
                val action = KeyboardAction(ActionType.INPUT)
                parseActionAttributes(action)
                val text = parser.nextText()
                return if (text == null) {
                    KeyboardAction(ActionType.NOOP)
                } else {
                    action.text = text
                    action.altColor = true
                    action
                }
            }
            "Continue" -> return KeyboardAction(ActionType.CONTINUE)
            "Pass" -> return KeyboardAction(ActionType.PASS)
            "None" -> return KeyboardAction(ActionType.NOOP)
            else -> return null
        }
    }

    /*
    fun parseActionArray(actionArray: JSONArray?): KeyboardAction? {
        actionArray ?: return null
        val stateAction = KeyboardStateAction()
        for (actionIndex in 0 until actionArray.length()) {
            val actionObj = actionArray.getJSONObject(actionIndex)
            val action = parseActionObj(actionObj)

            val stateSetString = actionObj.optString("condition","")

            if (stateSetString == "") {
                stateAction.defaultAction = action
            } else {
                val statesByName = stateSetString.split(",")
                val set = mutableSetOf<KeyboardModifierState>()
                for (stateName in statesByName) {
                    val stateId = KeyboardModifierState.getByName(stateName)
                    stateId?.let {
                        set.add(it)
                    }
                }
                stateAction.addState(set, action)
            }
        }
        stateAction.setState(keyboardView.stateSet)
        return stateAction
    }*/

    fun parseActions(bindCat: Int, bindings: BindingsConfig){
        val startDepth = parser.depth

        var divisions = 0
        while (!(parser.eventType == XmlPullParser.END_TAG && parser.depth <= startDepth)) {
            if (parser.next() == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "ActionList" -> skip(parser)
                    else -> {
                        val action = parseAction()
                        action?.let {
                            when (bindCat) {
                                ONPRESS -> bindings.onPressAction = action
                                ONSWIPE -> {
                                    bindings.onSwipeActions.add(action)
                                    divisions++
                                    bindings.divisions = divisions
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parseBindingCategory(){
        parser.require(XmlPullParser.START_TAG, ns, "Bindings")
        val startDepth = parser.depth

        val name = parser.getAttributeValue(null, "name")
        if (name == null) {
            skip(parser)
            return
        }

        val bindings = BindingsConfig()

        while (!(parser.eventType == XmlPullParser.END_TAG && parser.depth <= startDepth)) {
            if (parser.next() == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "OnPress" -> {
                        parseActions(ONPRESS, bindings)
                    }
                    "OnSwipe" -> {
                        val roll = parser.getAttributeValue(null, "roll").toFloatOrNull()
                        roll?.let{ bindings.roll = roll }
                        parseActions(ONSWIPE, bindings)
                        bindings.updateSwipeZones()
                    }
                }
            }
        }

        layer!!.binds.put(name, bindings)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parseLayer(){
        parser.require(XmlPullParser.START_TAG, ns, "BindingsLayer")
        val startDepth = parser.depth

        val name = parser.getAttributeValue(null, "name")
        if (name == null) {
            skip(parser)
            return
        }

        val rootAttr = parser.getAttributeValue(null, "root")
        val isRoot = if (rootAttr == null) false else rootAttr.toBoolean()

        layer = BindingsLayer(name)
        layer!!.isRoot = isRoot

        while (!(parser.eventType == XmlPullParser.END_TAG && parser.depth <= startDepth)) {
            if (parser.next() == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "Bindings" -> {
                        parseBindingCategory()
                    }
                }
            }
        }
    }

    fun parse(): BindingsLayer? {
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            if (parser.next() == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "BindingsLayer" -> {
                        parseLayer()
                    }
                }
            }
        }
        return layer
    }
}