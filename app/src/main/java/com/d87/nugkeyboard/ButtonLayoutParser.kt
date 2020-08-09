package com.d87.nugkeyboard

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class ButtonLayoutParser(val parser: XmlPullParser) {
    var keyConfig: ArrayList<ButtonConfig> = arrayListOf()
    val ns: String? = null
    var buttonIndex: Int = 0

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

    fun parseButton(){
        val id = parser.getAttributeValue(null, "id")
        if (id == null) {
            skip(parser)
            return
        }

        val key = ButtonConfig(id)

        for (i in 0 until parser.attributeCount) {
            val name = parser.getAttributeName(i)
            val value = parser.getAttributeValue(i)
            when (name) {
                "width" -> key.width = value.toFloat()
                "height" -> key.height = value.toFloat()
                "x" -> key.x = value.toFloat()
                "y" -> key.y = value.toFloat()
                "primary" -> key.isAccented = value.toBoolean()
                "altColor" -> key.isAltColor = value.toBoolean()
                //"hideRadials" -> key.
            }
        }

        parser.nextTag()
        keyConfig.add(key)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parseLayout(){
        parser.require(XmlPullParser.START_TAG, ns, "ButtonLayout")
        val startDepth = parser.depth
        // TODO: Wipe if exists
        while (!(parser.eventType == XmlPullParser.END_TAG && parser.depth <= startDepth)) {
            if (parser.next() == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "Button" -> {
                        parseButton()
                    }
                }
            }
        }
    }

    fun parse(): ArrayList<ButtonConfig> {
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            if (parser.next() == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "ButtonLayout" -> {
                        parseLayout()
                    }
                }
            }
        }
        return keyConfig
    }
}