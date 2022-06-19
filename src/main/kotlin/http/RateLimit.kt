package com.github.hank9999.mcbot.http

import http.types.RateLimitData

class RateLimit {
    private val rateLimitInfo: MutableMap<String, RateLimitData> = mutableMapOf()

    fun getSleepTime(bucketRaw: String): Long {
        val bucket = bucketRaw.lowercase()
        return if (!rateLimitInfo.containsKey(bucket)) {
            0L
        } else if (rateLimitInfo[bucket]!!.reset == 0) {
            0L
        } else if (rateLimitInfo[bucket]!!.remaining == 0) {
            (rateLimitInfo[bucket]!!.reset * 1000).toLong()
        } else {
            val sleepTime = rateLimitInfo[bucket]!!.reset.toFloat() / rateLimitInfo[bucket]!!.remaining.toFloat()
            val sleepTimeThree = String.format("%.3f", sleepTime).toFloat()
            (sleepTimeThree * 1000).toLong()
        }
    }

    fun updateRateLimitInfo(bucketRaw: String, limit: Int, remaining: Int, reset: Int) {
        val bucket = bucketRaw.lowercase()
        if (!rateLimitInfo.containsKey(bucket)) {
            rateLimitInfo[bucket] = RateLimitData(limit, remaining, reset)
        } else {
            rateLimitInfo[bucket]!!.limit = limit
            rateLimitInfo[bucket]!!.remaining = remaining
            rateLimitInfo[bucket]!!.reset = reset
        }
    }
}