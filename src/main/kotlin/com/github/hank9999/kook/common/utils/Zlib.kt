package com.github.hank9999.kook.common.utils

import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

object Zlib {
    fun decompress(content: ByteArray): String {
        val inflater = if (content.isNotEmpty() && content[0] == 0x78.toByte()) {
            Inflater()          // zlib-wrapped (RFC 1950)
        } else {
            Inflater(true)      // raw deflate (RFC 1951)
        }
        return InflaterInputStream(content.inputStream(), inflater)
            .bufferedReader().use { it.readText() }
    }
}