package com.github.hank9999.kook.http.types

data class RateLimitData(
    var limit: Int = 120,
    var remaining: Int = 120,
    var reset: Int = 0
)