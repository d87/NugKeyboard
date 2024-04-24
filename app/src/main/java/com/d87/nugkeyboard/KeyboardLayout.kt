package com.d87.nugkeyboard

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.graphics.drawable.DrawableCompat
import com.d87.nugkeyboard.R

data class ButtonConfig(val id: String) {
    // These are not absolute values but relative to keyboard dimensions
    var x: Float = 0f
    var y: Float = 0f
    var height: Float = 0.25f
    var width: Float = 0.25f
    var isAccented = false
    var isAltColor = false
}

class BindingsLayer(val name: String) {
    val binds: MutableMap<String, BindingsConfig> = mutableMapOf()
    var isRoot = false
    var isActive = false
}

class BindingsStack(rootLayer: BindingsLayer) {
    val layers = arrayListOf<BindingsLayer>(rootLayer)
    init {
        layers[0].isActive = true
    }

    fun add(new: BindingsLayer, defaultState: Boolean = false) {
        new.isActive = defaultState
        layers.add(0, new)
    }

    fun ToggleLayer(layerName: String) {
        for (layer in layers) {
            if (layer.name == layerName) {
                layer.isActive = !layer.isActive
                return
            }
        }
    }

    fun getActiveLayer(): BindingsLayer { // First active layer
        for (layer in layers) {
            if (layer.isActive) return layer
        }
        return layers.last()
    }

    fun getActiveBindingsForButton(id: String): BindingsConfig? {
        val active = getActiveLayer()
        val binds = active.binds[id]
        return binds
    }

    fun getMainAction(id: String): KeyboardAction {
        for (layer in layers) {
            if (layer.isActive) {
                layer.binds[id]?.let {
                    // PASS action type skips to the next layer
                    // TODO: Disallow PASS on root layer or return empty action
                    if (it.getMainAction().type != ActionType.PASS) {
                        return it.getMainAction()
                    }
                }
            }
        }
        return KeyboardAction(ActionType.NOOP)
    }

    fun getActionByAngle(id: String, angle: Float): KeyboardAction? {
        for (layer in layers) {
            if (layer.isActive) {
                layer.binds[id]?.let {
                    // PASS action type skips to the next layer
                    // TODO: Disallow PASS on root layer or return empty action
                    val action = it.getActionByAngle(angle)
                    if (action != null && action.type != ActionType.PASS) {
                        return action
                    }
                }
            }
        }
        return KeyboardAction(ActionType.NOOP)
    }
}


class KeyboardLayout(keyboardView: NugKeyboardView, val buttonLayoutResourceId: Int, val bindingsRootLayerID: Int, val bindingsNumericLayerID: Int) {
    var keyboardView = keyboardView

    var isLoaded = false

    var bindings: BindingsStack? = null

    fun load() {
        if (isLoaded) return

        val newConfig = ButtonLayoutParser(keyboardView.resources.getXml(buttonLayoutResourceId)).parse() // R.xml.button_layout
        keyConfig = newConfig

        val rootLayer = BindingsParser(this, keyboardView.resources.getXml(bindingsRootLayerID)).parse() // R.xml.bindings_ru
        rootLayer ?: throw IllegalStateException()
        val layerStack = BindingsStack(rootLayer!!)

        val numericLayer = BindingsParser(this, keyboardView.resources.getXml(bindingsNumericLayerID)).parse() // R.xml.bindings_numeric
        numericLayer ?: throw IllegalStateException()
        layerStack.add(numericLayer, false)

        bindings = layerStack


        isLoaded = true
    }

    // These are virtual pixel units for setting up layout and its aspect ratio, not even dps
    // They get mapped onto dips, and if it's a phone this dips value should snap to phone's width
    var layoutWidth = 1000f
    var layoutHeight = 930f

    var viewHeight: Float? = null;
    var viewWidth: Float? = null;

    //var themeColors = KeyboardThemeColors()
    var theme: KeyboardTheme = KeyboardTheme(run {
        val darkColors = KeyboardThemeColors()
        darkColors.backgroundColor = Color.parseColor("#000000")

        darkColors.headKeyPrimaryTextColor = Color.parseColor("#878788")
        darkColors.radialKeyPrimaryTextColor = Color.parseColor("#9e252b")
        darkColors.radialKeySecondaryTextColor = Color.parseColor("#638dfe")

        darkColors.normalButtonColor = Color.parseColor("#151515")
        darkColors.normalButtonColorAlt = Color.parseColor("#0d0d0d")
        darkColors.normalButtonColorHighlight = Color.parseColor("#d35c92")

        darkColors.trailColorHighlight = Color.parseColor("#d37cb2")

        darkColors.accentButtonColor = Color.parseColor("#272727")
        darkColors.accentButtonColorAlt = Color.parseColor("#1d1d1d")
        darkColors.accentButtonColorHighlight = Color.parseColor("#4c4c4c")


        val lightColors = KeyboardThemeColors()
        lightColors.backgroundColor = Color.parseColor("#9d9fa1")

        lightColors.headKeyPrimaryTextColor = Color.parseColor("#707070")
        lightColors.radialKeyPrimaryTextColor = Color.parseColor("#bd7593")
        lightColors.radialKeySecondaryTextColor = Color.parseColor("#b97fbd")

        lightColors.normalButtonColor = Color.parseColor("#eceff1")
        lightColors.normalButtonColorAlt = Color.parseColor("#e1e4e5")
        lightColors.normalButtonColorHighlight = Color.parseColor("#f0f0f0")

        lightColors.trailColorHighlight = Color.parseColor("#d37cb2")

        lightColors.accentButtonColor = Color.parseColor("#d4d8d9")
        lightColors.accentButtonColorAlt = Color.parseColor("#d4d8d9")
        lightColors.accentButtonColorHighlight = Color.parseColor("#f6fec8")

        lightColors
    }, keyboardView.resources.displayMetrics.density)
    var keyConfig: ArrayList<ButtonConfig> = arrayListOf()
    var keys: ArrayList<SwipeButton> = arrayListOf()

    fun resize(width: Float, height: Float, displeyDensity: Float) {
        viewHeight = height
        viewWidth = width
        val lp = width/layoutWidth

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
        DrawableCompat.setTint(wrappedDrawable, theme.colors.radialKeyPrimaryTextColor)
        DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.SRC_IN)
        return wrappedDrawable
    }

    private val iconMap: Map<String, Drawable?> = mapOf(
        Pair("RETURN", makeTintedDrawable(R.drawable.ic_keyboard_return_black_24dp)),
        Pair("BACKSPACE", makeTintedDrawable(R.drawable.ic_backspace_black_24dp)),
        Pair("CAPS_UP", makeTintedDrawable(R.drawable.ic_keyboard_arrow_up_black_24dp)),
        Pair("SPACE", makeTintedDrawable(R.drawable.ic_space_bar_black_24dp))
    )
    fun getDrawableByName(iconName: String): Drawable? {
        return iconMap[iconName]
    }

    fun makeKeys() {
        val newKeys: ArrayList<SwipeButton> = arrayListOf()

        loop@ for (btnConf in keyConfig) {
            val key = RectSwipeButton(this, btnConf)
            /*val key = when(btnConf.type) {
                "Rect" -> RectSwipeButton(this, btnConf)
                //"Round" -> RoundSwipeButton(this, btnConf)
                else -> continue@loop
            }*/
            newKeys.add(key)
        }
        keys = newKeys
    }
}