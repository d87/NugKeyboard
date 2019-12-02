package com.d87.nugkeyboard

import org.json.JSONObject
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.graphics.drawable.DrawableCompat
import org.json.JSONArray
import org.json.JSONException
import java.io.InputStream

data class ButtonConfig(val id: Int) {
    //val id: Int = id
    // These are not absolute values but relative to keyboard dimensions
    var x: Float = 0f
    var y: Float = 0f
    var height: Float = 0.25f
    var width: Float = 0.25f
    var isAccented = false
    var isAltColor = false
    var enableKeyRepeat = true
    //var _displayDensity: Float = 0f
    var roll: Float = 0f // Angle to adjust divisions
    var type: String = "Normal"
    var divisions: Int = 4
    var onPressAction: KeyboardAction = KeyboardAction(ActionType.NOOP)
    var onTouchDownAction: KeyboardAction? = null
    var onTouchUpAction: KeyboardAction? = null
    var onSwipeActions = arrayListOf<KeyboardAction>()
}

class KeyboardLayout(keyboardView: NugKeyboardView, file: InputStream) {
    var keyboardView = keyboardView

    var isLoaded = false
    var layoutFile: InputStream = file
    //constructor(keyboardView: NugKeyboardView, ): this(keyboardView) {
    //    layoutFile = file
    //}
    fun load() {
        if (isLoaded) return

        val JSON = layoutFile.bufferedReader().use { it.readText() }
        importJSON(JSON)
        isLoaded = true
    }

    // These are virtual pixel units for setting up layout and it's aspect ratio, not even dps
    // They get mapped onto dips, and if it's a phone this dips value should snap to phone's width
    var layoutWidth = 1000f
    var layoutHeight = 930f

    var viewHeight: Float? = null;
    var viewWidth: Float? = null;

    var theme: KeyboardTheme = KeyboardTheme()
    var keyConfig: ArrayList<ButtonConfig> = arrayListOf()
    var keys: ArrayList<SwipeButton> = arrayListOf()


    init {
        theme.backgroundColor = Color.parseColor("#000000")

        theme.headKeyPrimaryTextColor = Color.parseColor("#878788")
        theme.radialKeyPrimaryTextColor = Color.parseColor("#9e252b")
        theme.radialKeySecondaryTextColor = Color.parseColor("#55259e")

        theme.normalButtonColor = Color.parseColor("#151515")
        theme.normalButtonColorAlt = Color.parseColor("#0d0d0d")
        theme.normalButtonColorHighlight = Color.parseColor("#d35c92")

        theme.trailColorHighlight = Color.parseColor("#d37cb2")

        theme.accentButtonColor = Color.parseColor("#272727")
        theme.accentButtonColorAlt = Color.parseColor("#1d1d1d")
        theme.accentButtonColorHighlight = Color.parseColor("#4c4c4c")
    }

    var primaryTextSize: Float = 20f
    val primaryTextPaint: TextPaint = TextPaint().apply {
        typeface = Typeface.DEFAULT_BOLD
        flags = Paint.ANTI_ALIAS_FLAG
        textAlign = Paint.Align.CENTER
        color = theme.headKeyPrimaryTextColor
        textSize = 60f
    }
    val secondaryTextPaint: TextPaint = TextPaint().apply {
        typeface = Typeface.DEFAULT_BOLD
        flags = Paint.ANTI_ALIAS_FLAG
        textAlign = Paint.Align.CENTER
        color = theme.radialKeyPrimaryTextColor
        textSize = 40f
    }
    val highlightPaint: Paint = Paint().apply{
        style = Paint.Style.FILL
        color = theme.normalButtonColorHighlight
    }
    val highlightSwipeTrailPaint: Paint = Paint().apply{
        style = Paint.Style.STROKE
        strokeWidth = 15f
        color = theme.trailColorHighlight
    }
    val normalButtonPaint: Paint = Paint().apply{
        style = Paint.Style.FILL
        color = theme.normalButtonColor
    }
    val accentButtonPaint: Paint = Paint().apply{
        style = Paint.Style.FILL
        color = theme.accentButtonColor
    }
    val normalAltButtonPaint: Paint = Paint().apply{
        style = Paint.Style.FILL
        color = theme.normalButtonColorAlt
    }
    val accentAltButtonPaint: Paint = Paint().apply{
        style = Paint.Style.FILL
        color = theme.accentButtonColorAlt
    }

    fun resize(width: Float, height: Float, displeyDensity: Float) {
        viewHeight = height
        viewWidth = width
        val lp = width/layoutWidth
        primaryTextPaint.textSize = primaryTextSize*displeyDensity

        for (key in keys) {
            key.x = key.config.x * lp
            key.y = key.config.y * lp
            key.width = key.config.width * lp
            key.height = key.config.height * lp
            key.onResize()
        }
    }


    private fun makeTintedDrawable(resId: Int): Drawable? {
        val drawable = getDrawable(keyboardView.context, resId)
        drawable ?: return null
        var wrappedDrawable = drawable.mutate();
        wrappedDrawable = DrawableCompat.wrap(wrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, theme.radialKeyPrimaryTextColor)
        DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.SRC_IN)
        return wrappedDrawable
    }

    private val iconMap: Map<String, Drawable?> = mapOf(
        Pair("#!RETURN", makeTintedDrawable(R.drawable.ic_keyboard_return_black_24dp)),
        Pair("#!BACKSPACE", makeTintedDrawable(R.drawable.ic_backspace_black_24dp)),
        Pair("#!CAPS_UP", makeTintedDrawable(R.drawable.ic_keyboard_arrow_up_black_24dp)),
        Pair("#!SPACE", makeTintedDrawable(R.drawable.ic_space_bar_black_24dp))
    )
    fun getDrawableByName(iconName: String): Drawable? {
        return iconMap[iconName]
    }


    private fun parseActionObj(obj: JSONObject?): KeyboardAction {
        obj ?: return KeyboardAction(ActionType.NOOP)

        val actionString = obj.optString("action","NOOP")

        if (actionString == "CONTINUE") {
            return KeyboardAction(ActionType.CONTINUE)
        }

        val cmdList = actionString.split(":")
        var actionName = cmdList[0]

        var _savedArg: String? = null
        if (actionName.startsWith('&') || actionName.startsWith('&') || actionName.startsWith('@')) {
            _savedArg = actionName
            actionName = "INPUT"
        }

        val actionId = ActionType.getByName(actionName) ?: ActionType.NOOP

        val action = KeyboardAction(actionId)

        if (actionName == "INPUT") {
            var keyString = _savedArg ?: cmdList[1]
            when(keyString[0]) {
                '#' ->  {
                    action.keyCode = keyString.substring(1).toIntOrNull()
                }
                '&' ->  {
                    action.text = keyString.substring(1)
                }
                '@' ->  {
                    val keycodeName = keyString.substring(1)
                    action.keyCode = KeyCodes[keycodeName]
                }
            }
        }

        val iconString = try { obj.getString("icon") } catch (e: JSONException) { null }
        iconString?.let{ action.icon = getDrawableByName(it) }

        var scale: Double = try { obj.getDouble("scale") } catch (e: JSONException) { 1.0 }
        action.scale = scale.toFloat()

        var isHidden = try { obj.getBoolean("isHidden") } catch (e: JSONException) { false }
        action.isHidden = isHidden

        return action
    }

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
        stateAction.setState(keyboardView.stateSet) // TODO: Redundant
        return stateAction
    }

    fun importJSON(jsonStr: String) {

        var jsonObj: JSONObject?
        var keyConfigArray: JSONArray?
        val newKeyConfig: ArrayList<ButtonConfig> = arrayListOf()

        try {
            jsonObj = JSONObject(jsonStr)
            keyConfigArray = jsonObj.getJSONArray("keyConfig")
        } catch (e: JSONException) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            return
        }

        for (keyIndex in 0 until keyConfigArray.length() ) {
            val keyJObj = keyConfigArray.getJSONObject(keyIndex)
            Log.d("JSON2", keyJObj.toString())

            val btnConf = ButtonConfig(keyIndex)

            val x: Double? = keyJObj.optDouble("x", 0.0)
            val y: Double? = keyJObj.optDouble("y", 0.0)
            val width: Double? = try { keyJObj.getDouble("width") } catch (e: JSONException) { null }
            val height: Double? = try { keyJObj.getDouble("height") } catch (e: JSONException) { null }
            val roll: Double? = try { keyJObj.getDouble("roll") } catch (e: JSONException) { null }
            val type: String? = try { keyJObj.getString("type") } catch (e: JSONException) { null }

            var onPressAction = parseActionArray(keyJObj.optJSONArray("onPressAction"))
            onPressAction = onPressAction ?: parseActionObj(keyJObj.optJSONObject("onPressAction"))

            var onTouchDownAction = parseActionObj(keyJObj.optJSONObject("onTouchDownAction"))
            var onTouchUpAction = parseActionObj(keyJObj.optJSONObject("onTouchUpAction"))

            val isAccented: Boolean? = try { keyJObj.getBoolean("isAccented") } catch (e: JSONException) { null }
            val isAltColor: Boolean? = try { keyJObj.getBoolean("isAltColor") } catch (e: JSONException) { null }

            x?.let{ btnConf.x = it.toFloat() }
            y?.let{ btnConf.y = it.toFloat() }
            width?.let{ btnConf.width = it.toFloat() }
            height?.let{ btnConf.height = it.toFloat() }
            roll?.let{ btnConf.roll = it.toFloat() }
            type?.let{ btnConf.type = it }
            onPressAction?.let{ btnConf.onPressAction = it }
            onTouchDownAction?.let{ btnConf.onTouchDownAction = it}
            onTouchUpAction?.let{ btnConf.onTouchUpAction = it}

            isAccented?.let{ btnConf.isAccented = it }
            isAltColor?.let{ btnConf.isAltColor = it }

            val onSwipeJSONArray: JSONArray? = try { keyJObj.getJSONArray("onSwipeActions") } catch (e: JSONException) { null }
            if (onSwipeJSONArray != null) {
                val divisions = onSwipeJSONArray.length()
                btnConf.divisions = divisions

                val newSwipeActions: ArrayList<KeyboardAction> = arrayListOf()
                for (swipeArrayIndex in 0 until divisions ) {
                    var action = parseActionArray(onSwipeJSONArray.optJSONArray(swipeArrayIndex))
                    action = action ?: parseActionObj(onSwipeJSONArray.optJSONObject(swipeArrayIndex))

                    try {
                        newSwipeActions.add(action)
                    } catch (e: Exception) {
                        Log.e("ButtonConfig ArrayList", "Error " + e.toString());
                    }
                }
                btnConf.onSwipeActions = newSwipeActions
            }
            newKeyConfig.add(btnConf)
        }

        keyConfig = newKeyConfig
    }

    fun makeKeys() {
        val newKeys: ArrayList<SwipeButton> = arrayListOf()

        loop@ for (btnConf in keyConfig) {
            val key = when(btnConf.type) {
                "Rect" -> RectSwipeButton(this, btnConf)
                //"Round" -> RoundSwipeButton(this, btnConf)
                else -> continue@loop
            }
            newKeys.add(key)
        }
        keys = newKeys
    }
}