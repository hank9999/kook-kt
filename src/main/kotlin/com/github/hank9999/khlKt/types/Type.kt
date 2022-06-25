package com.github.hank9999.khlKt.types

class Type {
    enum class Click(val click: String) {
        NA(""),
        LINK("link"),
        RETURN_VAL("return-val")
    }

    enum class CountdownMode(val mode: String) {
        DAY("day"),
        HOUR("hour"),
        SECOND("second")
    }

    enum class File(val type: String) {
        FILE("file"),
        AUDIO("audio"),
        VIDEO("video")
    }

    enum class Theme(val theme: String) {
        NA(""),
        PRIMARY("primary"),
        SECONDARY("secondary"),
        SUCCESS("success"),
        DANGER("danger"),
        WARNING("warning"),
        INFO("info"),
        NONE("none")
    }

    enum class Text(val type: String) {
        PLAIN("plain-text"),
        KMD("kmarkdown")
    }

    enum class Size(val size: String) {
        NA(""),
        XS("xs"),
        SM("sm"),
        MD("md"),
        LG("lg")
    }

    enum class SectionMode(val mode: String) {
        RIGHT("right"),
        LEFT("left")
    }
}