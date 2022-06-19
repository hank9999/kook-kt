package com.github.hank9999.khlKt.card

import com.github.hank9999.khlKt.card.exceptions.CardException
import com.github.hank9999.khlKt.types.types.CountdownMode
import com.github.hank9999.khlKt.types.types.FileTypes
import com.github.hank9999.khlKt.types.types.SectionMode
import com.github.hank9999.khlKt.types.types.TextTypes
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class Module {
    abstract fun toJsonObject(): JsonObject
    val reInviteCode = Regex("^[a-zA-Z0-9]{6}$")
    val reInviteLink = Regex("^https://kaihei\\.co/[a-zA-Z0-9]{6}$")
    private val logger: Logger = LoggerFactory.getLogger(Module::class.java)

    class Header(val text: Element.Text) : Module() {
        init {
            if (text.type != TextTypes.PLAIN) throw CardException("Header, only plain-text for header")
            if (text.content.length > 100) throw CardException("Header, only 100 Chinese character for header")
        }
        override fun toJsonObject(): JsonObject {
            return buildJsonObject {
                put("type", "section")
                put("text", text.toJsonObject())
            }
        }
    }

    class Section(val mode: SectionMode = SectionMode.LEFT) : Module() {
        var text: Element? = null
        var accessory: Element? = null
        constructor(text: Element.Text, mode: SectionMode = SectionMode.LEFT) : this(mode) {
            this.text = text
        }
        constructor(text: Element.Paragraph, mode: SectionMode = SectionMode.LEFT) : this(mode) {
            this.text = text
        }
        constructor(accessory: Element.Image, mode: SectionMode = SectionMode.LEFT) : this(mode) {
            this.accessory = accessory
        }
        constructor(accessory: Element.Button, mode: SectionMode = SectionMode.LEFT) : this(mode) {
            this.accessory = accessory
        }
        constructor(text: Element.Text, accessory: Element.Image, mode: SectionMode = SectionMode.LEFT) : this(mode) {
            this.text = text
            this.accessory = accessory
        }
        constructor(text: Element.Text, accessory: Element.Button, mode: SectionMode = SectionMode.LEFT) : this(mode) {
            this.text = text
            this.accessory = accessory
        }
        constructor(text: Element.Paragraph, accessory: Element.Image, mode: SectionMode = SectionMode.LEFT) : this(mode) {
            this.text = text
            this.accessory = accessory
        }
        constructor(text: Element.Paragraph, accessory: Element.Button, mode: SectionMode = SectionMode.LEFT) : this(mode) {
            this.text = text
            this.accessory = accessory
        }
        override fun toJsonObject(): JsonObject {
            if (text == null && accessory == null) throw CardException("Section, no element")
            return buildJsonObject {
                put("type", "section")
                if (text != null) put("text", text!!.toJsonObject())
                if (accessory != null) put("accessory", accessory!!.toJsonObject())
            }
        }
    }

    class ImageGroup(vararg args: Element.Image) : Module() {
        private val elements: MutableList<Element.Image> = mutableListOf()
        init {
            for (item in args) append(item)
        }

        fun append(element: Element.Image): ImageGroup {
            if (length() >= 9) throw CardException("ImageGroup, elements max length 9")
            elements.add(element)
            return this
        }

        fun removeAt(index: Int): ImageGroup {
            elements.removeAt(index)
            return this
        }

        fun pop(index: Int): Element.Image {
            val element = get(index)
            elements.removeAt(index)
            return element
        }

        fun clear(): ImageGroup {
            elements.clear()
            return this
        }

        fun get(index: Int): Element.Image {
            return elements[index]
        }

        fun getAll(): MutableList<Element.Image> {
            return elements
        }

        fun length(): Int {
            return elements.size
        }

        override fun toJsonObject(): JsonObject {
            if (length() < 1) throw CardException("ImageGroup, elements min length 1")
            return buildJsonObject {
                put("type", "image-group")
                putJsonArray("elements") {
                    for (item in elements) add(item.toJsonObject())
                }
            }
        }
    }

    class Container(vararg args: Element.Image) : Module() {
        private val elements: MutableList<Element.Image> = mutableListOf()
        init {
            for (item in args) append(item)
        }

        fun append(element: Element.Image): Container {
            if (length() >= 9) throw CardException("Container, elements max length 9")
            elements.add(element)
            return this
        }

        fun removeAt(index: Int): Container {
            elements.removeAt(index)
            return this
        }

        fun pop(index: Int): Element.Image {
            val element = get(index)
            elements.removeAt(index)
            return element
        }

        fun clear(): Container {
            elements.clear()
            return this
        }

        fun get(index: Int): Element.Image {
            return elements[index]
        }

        fun getAll(): MutableList<Element.Image> {
            return elements
        }

        fun length(): Int {
            return elements.size
        }

        override fun toJsonObject(): JsonObject {
            if (length() < 1) throw CardException("Container, elements min length 1")
            return buildJsonObject {
                put("type", "container")
                putJsonArray("elements") {
                    for (item in elements) add(item.toJsonObject())
                }
            }
        }
    }

    class ActionGroup(vararg args: Element.Button) : Module() {
        private val elements: MutableList<Element.Button> = mutableListOf()
        init {
            for (item in args) append(item)
        }

        fun append(element: Element.Button): ActionGroup {
            if (length() >= 4) throw CardException("ActionGroup, elements max length 4")
            elements.add(element)
            return this
        }

        fun removeAt(index: Int): ActionGroup {
            elements.removeAt(index)
            return this
        }

        fun pop(index: Int): Element.Button {
            val element = get(index)
            elements.removeAt(index)
            return element
        }

        fun clear(): ActionGroup {
            elements.clear()
            return this
        }

        fun get(index: Int): Element.Button {
            return elements[index]
        }

        fun getAll(): MutableList<Element.Button> {
            return elements
        }

        fun length(): Int {
            return elements.size
        }

        override fun toJsonObject(): JsonObject {
            if (length() < 1) throw CardException("ActionGroup, elements min length 1")
            return buildJsonObject {
                put("type", "action-group")
                putJsonArray("elements") {
                    for (item in elements) add(item.toJsonObject())
                }
            }
        }
    }

    class Context(vararg args: Element) : Module() {
        private val elements: MutableList<Element> = mutableListOf()
        init {
            for (item in args) append(item)
        }

        fun append(element: Element): Context {
            if (element !is Element.Text && element !is Element.Image) throw CardException("Context, element type error, only Text and Image")
            if (length() >= 10) throw CardException("Context, elements max length 10")
            elements.add(element)
            return this
        }

        fun removeAt(index: Int): Context {
            elements.removeAt(index)
            return this
        }

        fun pop(index: Int): Element {
            val element = get(index)
            elements.removeAt(index)
            return element
        }

        fun clear(): Context {
            elements.clear()
            return this
        }

        fun get(index: Int): Element {
            return elements[index]
        }

        fun getAll(): MutableList<Element> {
            return elements
        }

        fun length(): Int {
            return elements.size
        }

        override fun toJsonObject(): JsonObject {
            if (length() < 1) throw CardException("Context, elements min length 1")
            return buildJsonObject {
                put("type", "context")
                putJsonArray("elements") {
                    for (item in elements) add(item.toJsonObject())
                }
            }
        }
    }

    class Divider() : Module() {
        override fun toJsonObject(): JsonObject {
            return buildJsonObject {
                put("type", "divider")
            }
        }
    }

    class File(val type: FileTypes, val src: String, val title: String, val cover: String = "") : Module() {
        override fun toJsonObject(): JsonObject {
            return buildJsonObject {
                put("type", type.type)
                put("src", src)
                put("title", title)
                if (cover.isNotEmpty() && type == FileTypes.AUDIO) put("cover", cover)
            }
        }
    }

    class Countdown(val mode: CountdownMode, val endTime: Long, val startTime: Long = 0L) : Module() {
        override fun toJsonObject(): JsonObject {
            return buildJsonObject {
                put("type", mode.mode)
                put("endTime", endTime)
                if (startTime != 0L && mode == CountdownMode.SECOND) put("startTime", startTime)
            }
        }
    }

    class Invite(val code: String) : Module() {
        init {
            if (!reInviteCode.matches(code) && !reInviteLink.matches(code)) throw CardException("Invite, code format error")
        }
        override fun toJsonObject(): JsonObject {
            return buildJsonObject {
                put("type", "invite")
                put("endTime", code)
            }
        }
    }
}