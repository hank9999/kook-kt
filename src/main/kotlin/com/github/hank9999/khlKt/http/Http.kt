package com.github.hank9999.khlKt.http

import com.github.hank9999.khlKt.http.types.HttpResponse
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull


class Http {
    companion object {

        fun get(url: String, headers: Map<String, String> = mapOf(), params: Map<String, String> = mapOf()): HttpResponse {
            val client = OkHttpClient()
            val httpBuilder: HttpUrl.Builder = url.toHttpUrlOrNull()!!.newBuilder()
            for (item in params.entries) {
                httpBuilder.addQueryParameter(item.key, item.value)
            }
            val builder = Request.Builder().url(httpBuilder.build())
            for (item in headers.entries) {
                builder.addHeader(item.key, item.value)
            }
            val request = builder.build()
            client.newCall(request).execute().use { resp ->
                return HttpResponse(resp.code, resp.body!!.string(), resp.headers.toMultimap())
            }
        }

        fun post(url: String, headers: Map<String, String> = mapOf(), data: RequestBody): HttpResponse {
            val client = OkHttpClient()
            val builder = Request.Builder().url(url).post(data)
            for (item in headers.entries) {
                builder.addHeader(item.key, item.value)
            }
            val request = builder.build()
            client.newCall(request).execute().use { resp ->
                return HttpResponse(resp.code, resp.body!!.string(), resp.headers.toMultimap())
            }
        }
    }
}