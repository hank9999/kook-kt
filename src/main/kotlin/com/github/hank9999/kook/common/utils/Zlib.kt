package com.github.hank9999.kook.common.utils

import java.util.zip.InflaterInputStream

object Zlib {
    fun decompress(content: ByteArray): String {
        return InflaterInputStream(content.inputStream()).bufferedReader().use { it.readText() }
    }
}