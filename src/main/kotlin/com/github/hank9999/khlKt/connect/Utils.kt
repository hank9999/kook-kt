package com.github.hank9999.khlKt.connect

import java.util.zip.InflaterInputStream

class Utils {
    companion object {
        fun decompressZlib(content: ByteArray): String {
            return InflaterInputStream(content.inputStream()).bufferedReader().use { it.readText() }
        }
    }
}