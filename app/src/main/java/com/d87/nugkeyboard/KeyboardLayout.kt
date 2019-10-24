package com.d87.nugkeyboard

import org.json.JSONObject
import android.graphics.Color
import android.util.Log
import android.view.View
import org.json.JSONArray
import org.json.JSONException

data class ButtonConfig(val id: Int) {
    //val id: Int = id
    // These are not absolute values but relative to keyboard dimensions
    var x: Float = 0f
    var y: Float = 0f
    var height: Float = 0.25f
    var width: Float = 0.25f
    //var _displayDensity: Float = 0f
    var roll: Float = 0f // Angle to adjust divisions
    var type: String = "Normal"
    var divisions: Int = 4
    var bindings = arrayListOf<String>()
}

class KeyboardLayout() {
    var aspectRatio: Float = 1f;
    var viewHeight: Float? = null;
    var viewWidth: Float? = null;
    var primaryTextColor = Color.MAGENTA
    var secondaryTextColor = Color.RED
    var primaryTextSize: Float = 20f
    var keyConfig: ArrayList<ButtonConfig> = arrayListOf()
    var keys: ArrayList<SwipeButton> = arrayListOf()

    fun resize(width: Float, height: Float, displeyDensity: Float) {
        viewHeight = height
        viewWidth = width

        for (key in keys) {
            key.x = key.config.x * width
            key.y = key.config.y * height
            key.width = key.config.width * width
            key.height = key.config.width * height
            key.onResize()
            key.primaryTextPaint.textSize = primaryTextSize*displeyDensity
        }
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
            val divisions: Int? = try { keyJObj.getInt("divisions") } catch (e: JSONException) { null }

            x?.let{ btnConf.x = it.toFloat() }
            y?.let{ btnConf.y = it.toFloat() }
            width?.let{ btnConf.width = it.toFloat() }
            height?.let{ btnConf.height = it.toFloat() }
            roll?.let{ btnConf.height = it.toFloat() }
            type?.let{ btnConf.type = it }
            divisions?.let{ btnConf.divisions = it }

            val bindingsArray: JSONArray? = try { keyJObj.getJSONArray("bindings") } catch (e: JSONException) { null }
            if (bindingsArray != null) {
                val newBindings: ArrayList<String> = arrayListOf()
                for (bindingIndex in 0 until btnConf.divisions ) {
                    val bindingStr: String = try { bindingsArray.getString(bindingIndex) } catch (e: JSONException) { "<REPEAT>" }
                    try {
                        newBindings.add(bindingStr)
                    } catch (e: Exception) {
                        Log.e("ButtonConfig ArrayList", "Error " + e.toString());
                    }
                }
                btnConf.bindings = newBindings
            }

            newKeyConfig.add(btnConf)
        }

        keyConfig = newKeyConfig
    }

    fun makeKeys(keyboardView: NugKeyboardView) {
        val newKeys: ArrayList<SwipeButton> = arrayListOf()

        loop@ for (btnConf in keyConfig) {
            //val key = SwipeButton(btnConf, keyboardView)
            val key = when(btnConf.type) {
                "Rect" -> RectSwipeButton(btnConf, keyboardView)
                "Round" -> RoundSwipeButton(btnConf, keyboardView)
                else -> continue@loop
            }
            newKeys.add(key)
        }
        keys = newKeys
    }
}