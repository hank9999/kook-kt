package com.github.hank9999.kook.card

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CardMessage(vararg args: Card) {
    private val cards: MutableList<Card> = mutableListOf()
    private val logger: Logger = LoggerFactory.getLogger(CardMessage::class.java)

    init {
        for (c in args) {
            if (c.length() != 0) this.append(c)
        }
    }

    fun append(card: Card): CardMessage {
        cards.add(card)
        return this
    }

    fun removeAt(index: Int): CardMessage {
        cards.removeAt(index)
        return this
    }

    fun pop(index: Int): Card {
        val card = get(index)
        cards.removeAt(index)
        return card
    }

    fun clear() {
        cards.clear()
    }

    fun get(index: Int): Card {
        return cards[index]
    }

    fun getAll(): MutableList<Card> {
        return cards
    }

    fun length(): Int {
        return cards.size
    }

    fun toJsonObject(): JsonArray {
        return buildJsonArray {
            for (card in cards) add(card.toJsonObject())
        }
    }

    override fun toString(): String {
        return toJsonObject().toString()
    }
}