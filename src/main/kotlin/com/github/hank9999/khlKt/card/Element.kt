package com.github.hank9999.khlKt.card

import com.github.hank9999.khlKt.card.exceptions.CardException
import com.github.hank9999.khlKt.types.Type
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class Element {
    abstract fun toJsonObject(): JsonObject
    private val logger: Logger = LoggerFactory.getLogger(Element::class.java)

    class Text(val content: String, val emoji: Boolean = true, val type: Type.Text = Type.Text.PLAIN) : Element() {
        override fun toJsonObject(): JsonObject {
            return buildJsonObject {
                put("type", type.type)
                put("content", content)
                if (!emoji) {
                    put("emoji", false)
                }
            }
        }
    }


    class Image(val src: String, val alt: String = "", val circle: Boolean = false) : Element() {
        var size = Type.Size.NA
        constructor(src: String, alt: String = "", circle: Boolean = false, size: Type.Size) : this(src, alt, circle) {
            this.size = size
        }
        override fun toJsonObject(): JsonObject {
            return buildJsonObject {
                put("type", "image")
                put("src", src)
                if (alt.isNotEmpty()) {
                    put("alt", alt)
                }
                if (circle) {
                    put("circle", true)
                }
                if (size != Type.Size.NA) {
                    put("size", size.size)
                }
            }
        }
    }

    class Button(val theme: Type.Theme = Type.Theme.NA, val text: Text, val value: String = "", val click: Type.Click = Type.Click.NA) : Element() {
        override fun toJsonObject(): JsonObject {
            return buildJsonObject {
                put("type", "button")
                if (theme != Type.Theme.NA) {
                    put("theme", theme.theme)
                }
                put("text", text.toJsonObject())
                if (value.isNotEmpty()) {
                    put("value", value)
                }
                if (click != Type.Click.NA) {
                    put("click", click.click)
                }
            }
        }
    }

    class Paragraph(var cols: Int, vararg args: Text) : Element() {
        private val fields: MutableList<Text> = mutableListOf()

        init {
            if (cols !in 1..3) throw CardException("Paragraph, cols error, only 1-3")
            for (item in args) append(item)
        }

        fun append(field: Text): Paragraph {
            if (length() >= 50) throw CardException("Paragraph, fields max length 50")
            fields.add(field)
            return this
        }

        fun removeAt(index: Int): Paragraph {
            fields.removeAt(index)
            return this
        }

        fun pop(index: Int): Text {
            val field = get(index)
            fields.removeAt(index)
            return field
        }

        fun clear(): Paragraph {
            fields.clear()
            return this
        }

        fun get(index: Int): Text {
            return fields[index]
        }

        fun getAll(): MutableList<Text> {
            return fields
        }

        fun length(): Int {
            return fields.size
        }

        override fun toJsonObject(): JsonObject {
            if (length() < 1) throw CardException("Paragraph, fields min length 1")
            return buildJsonObject {
                put("type", "paragraph")
                put("cols", cols)
                putJsonArray("fields") {
                    for (item in fields) add(item.toJsonObject())
                }
            }
        }
    }
}