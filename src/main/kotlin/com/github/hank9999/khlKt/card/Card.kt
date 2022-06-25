package com.github.hank9999.khlKt.card

import com.github.hank9999.khlKt.types.Type
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Card {
    val type = "card"
    private val modules: MutableList<Module> = mutableListOf()
    var theme = Type.Theme.NA
    var size = Type.Size.NA
    private val logger: Logger = LoggerFactory.getLogger(Card::class.java)

    constructor(size: Type.Size = Type.Size.NA, theme: Type.Theme = Type.Theme.NA) {
        this.size = size
        this.theme = theme
    }

    constructor(vararg args: Module) {
        for (m in args) {
            this.append(m)
        }
    }

    constructor(vararg args: Module, size: Type.Size = Type.Size.NA, theme: Type.Theme = Type.Theme.NA) {
        for (m in args) {
            this.append(m)
        }
        this.size = size
        this.theme = theme
    }

    constructor(size: Type.Size = Type.Size.NA, theme: Type.Theme = Type.Theme.NA, vararg args: Module) {
        for (m in args) {
            this.append(m)
        }
        this.size = size
        this.theme = theme
    }

    constructor(size: Type.Size = Type.Size.NA, vararg args: Module) {
        for (m in args) {
            this.append(m)
        }
        this.size = size
    }

    constructor(theme: Type.Theme = Type.Theme.NA, vararg args: Module) {
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
            if (theme != Type.Theme.NA) {
                put("theme", theme.theme)
            }
            if (size != Type.Size.NA) {
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