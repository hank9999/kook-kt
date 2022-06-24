package com.github.hank9999.khlKt.card

import com.github.hank9999.khlKt.types.types.Size
import com.github.hank9999.khlKt.types.types.Theme
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Card {
    val type = "card"
    private val modules: MutableList<Module> = mutableListOf()
    var theme = Theme.NA
    var size = Size.NA
    private val logger: Logger = LoggerFactory.getLogger(Card::class.java)

    constructor(size: Size = Size.NA, theme: Theme = Theme.NA) {
        this.size = size
        this.theme = theme
    }

    constructor(vararg args: Module) {
        for (m in args) {
            this.append(m)
        }
    }

    constructor(vararg args: Module, size: Size = Size.NA, theme: Theme = Theme.NA) {
        for (m in args) {
            this.append(m)
        }
        this.size = size
        this.theme = theme
    }

    constructor(size: Size = Size.NA, theme: Theme = Theme.NA, vararg args: Module) {
        for (m in args) {
            this.append(m)
        }
        this.size = size
        this.theme = theme
    }

    constructor(size: Size = Size.NA, vararg args: Module) {
        for (m in args) {
            this.append(m)
        }
        this.size = size
    }

    constructor(theme: Theme = Theme.NA, vararg args: Module) {
        for (m in args) {
            this.append(m)
        }
        this.theme = theme
    }

    fun append(module: Module): Card {
        modules.add(module)
        return this
    }

    fun removeAt(index: Int): Card {
        modules.removeAt(index)
        return this
    }

    fun pop(index: Int): Module {
        val module = get(index)
        modules.removeAt(index)
        return module
    }

    fun clear(): Card {
        modules.clear()
        return this
    }

    fun get(index: Int): Module {
        return modules[index]
    }

    fun getAll(): MutableList<Module> {
        return modules
    }

    fun length(): Int {
        return modules.size
    }

    fun toJsonObject(): JsonObject {
        return buildJsonObject {
            put("type", type)
            if (theme != Theme.NA) {
                put("theme", theme.theme)
            }
            if (size != Size.NA) {
                put("size", size.size)
            }
            putJsonArray("modules") {
                for (module in modules) add(module.toJsonObject())
            }
        }
    }

    override fun toString(): String {
        return toJsonObject().toString()
    }
}