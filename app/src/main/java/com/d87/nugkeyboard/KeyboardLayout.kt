package com.d87.nugkeyboard

import android.content.Context
import org.json.JSONObject
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.inputmethodservice.Keyboard
import android.text.TextPaint
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.graphics.drawable.DrawableCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import org.json.JSONArray
import org.json.JSONException

data class ButtonConfig(val id: Int) {
    //val id: Int = id
    // These are not absolute values but relative to keyboard dimensions
    var x: Float = 0f
    var y: Float = 0f
    var height: Float = 0.25f
    var width: Float = 0.25f
    var isAccented = false
    var isAltColor = false
    //var _displayDensity: Float = 0f
    var roll: Float = 0f // Angle to adjust divisions
    var type: String = "Normal"
    var divisions: Int = 4
    var mainKey: KeyboardAction = KeyboardAction("O")
    var radialKeys = arrayListOf<KeyboardAction>()
}

class KeyboardLayout(keyboardView: NugKeyboardView) {
    var _context: Context = keyboardView.context
    var keyboardView = keyboardView

    //var aspectRatio: Float = 1f;

    // These are virtual pixel units for setting up layout and it's aspect ratio, not even dps
    // They get mapped onto dips, and if it's a phone this dips value should snap to phone's width
    var layoutWidth = 1000f
    var layoutHeight = 930f

    var viewHeight: Float? = null;
    var viewWidth: Float? = null;

    var theme: KeyboardTheme = KeyboardTheme()
    var keyConfig: ArrayList<ButtonConfig> = arrayListOf()
    var keys: ArrayList<SwipeButton> = arrayListOf()

    //open fun initializeResources(context: Context, keyboardView: NugKeyboardView) {
    //    _context = context
    //}

    init {
        theme.backgroundColor = Color.parseColor("#000000")

        theme.headKeyPrimaryTextColor = Color.parseColor("#878788")
        theme.radialKeyPrimaryTextColor = Color.parseColor("#9e252b")
        theme.radialKeySecondaryTextColor = Color.parseColor("#55259e")

        theme.normalButtonColor = Color.parseColor("#151515")
        theme.normalButtonColorAlt = Color.parseColor("#0d0d0d")
        theme.normalButtonColorHighlight = Color.parseColor("#d35c92")

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
        Pair("#!SPACE", makeTintedDrawable(R.drawable.ic_space_bar_black_24dp))
    )
    fun getDrawableByName(iconName: String): Drawable? {
        return iconMap[iconName]
    }


    private fun parseActionObj(obj: JSONObject): KeyboardAction {
        val actionString = try { obj.getString("action") } catch (e: JSONException) { "%NOOP" }

        val action = KeyboardAction(actionString)

        val iconString = try { obj.getString("icon") } catch (e: JSONException) { null }
        iconString?.let{ action.icon = getDrawableByName(it) }

        var scale: Double = try { obj.getDouble("scale") } catch (e: JSONException) { 1.0 }
        action.scale = scale.toFloat()

        var isHidden = try { obj.getBoolean("isHidden") } catch (e: JSONException) { false }
        action.isHidden = isHidden

        return action
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

            val x: Double? = try { keyJObj.getDouble("x") } catch (e: JSONException) { null }
            val y: Double? = try { keyJObj.getDouble("y") } catch (e: JSONException) { null }
            val width: Double? = try { keyJObj.getDouble("width") } catch (e: JSONException) { null }
            val height: Double? = try { keyJObj.getDouble("height") } catch (e: JSONException) { null }
            val roll: Double? = try { keyJObj.getDouble("roll") } catch (e: JSONException) { null }
            val type: String? = try { keyJObj.getString("type") } catch (e: JSONException) { null }
            val mainKeyActionObj = try { keyJObj.getJSONObject("mainKey") } catch (e: JSONException) { null }
            val mainKeyAction = mainKeyActionObj?.let { parseActionObj(it) }
            val isAccented: Boolean? = try { keyJObj.getBoolean("isAccented") } catch (e: JSONException) { null }
            val isAltColor: Boolean? = try { keyJObj.getBoolean("isAltColor") } catch (e: JSONException) { null }

            x?.let{ btnConf.x = it.toFloat() }
            y?.let{ btnConf.y = it.toFloat() }
            width?.let{ btnConf.width = it.toFloat() }
            height?.let{ btnConf.height = it.toFloat() }
            roll?.let{ btnConf.height = it.toFloat() }
            type?.let{ btnConf.type = it }
            mainKeyAction?.let{ btnConf.mainKey = mainKeyAction }
            isAccented?.let{ btnConf.isAccented = it }
            isAltColor?.let{ btnConf.isAltColor = it }

            val radialArray: JSONArray? = try { keyJObj.getJSONArray("radialKeys") } catch (e: JSONException) { null }
            if (radialArray != null) {
                val divisions = radialArray.length()
                btnConf.divisions = divisions

                val newRadialKeys: ArrayList<KeyboardAction> = arrayListOf()
                for (radialIndex in 0 until divisions ) {
                    //val radialStr: String = try { radialArray.getString(radialIndex) } catch (e: JSONException) { "<REPEAT>" }
                    val actionObj = try { radialArray.getJSONObject(radialIndex) } catch (e: JSONException) { null }
                    if (actionObj == null) continue

                    val action = parseActionObj(actionObj)

                    try {
                        newRadialKeys.add(action)
                    } catch (e: Exception) {
                        Log.e("ButtonConfig ArrayList", "Error " + e.toString());
                    }
                }
                btnConf.radialKeys = newRadialKeys
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
                "Round" -> RoundSwipeButton(this, btnConf)
                else -> continue@loop
            }
            newKeys.add(key)
        }
        keys = newKeys
    }
}