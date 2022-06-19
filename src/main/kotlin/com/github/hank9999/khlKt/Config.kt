package com.github.hank9999.khlKt

data class Config(
    val token: String,
    val verify_token: String = "",
    val cmd_prefix: List<String> = listOf(".", "。", "/"),
    val host: String = "localhost",
    val port: Int = 3000,
    val path: String = "/webhook"
)