package com.github.hank9999.kook.http

import com.github.hank9999.kook.http.types.HttpResponse
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import com.github.hank9999.kook.http.coroutine.*

class Http {
    companion object {
        val client = OkHttpClient()

        fun get(url: String, headers: Map<String, String> = mapOf(), params: Map<String, String> = mapOf()): HttpResponse {
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
            val builder = Request.Builder().url(url).post(data)
            for (item in headers.entries) {
                builder.addHeader(item.key, item.value)
            }
            val request = builder.build()
            client.newCall(request).execute().use { resp ->
                return HttpResponse(resp.code, resp.body!!.string(), resp.headers.toMultimap())
            }
        }

        suspend fun aget(url: String, headers: Map<String, String> = mapOf(), params: Map<String, String> = mapOf()): HttpResponse {
            val httpBuilder: HttpUrl.Builder = url.toHttpUrlOrNull()!!.newBuilder()
            for (item in params.entries) {
                httpBuilder.addQueryParameter(item.key, item.value)
            }
            val builder = Request.Builder().url(httpBuilder.build())
            for (item in headers.entries) {
                builder.addHeader(item.key, item.value)
            }
            val request = builder.build()
            val resp = client.newCall(request).await()
            return HttpResponse(resp.code, resp.body!!.string(), resp.headers.toMultimap())
        }

        suspend fun apost(url: String, headers: Map<String, String> = mapOf(), data: RequestBody, params: Map<String, String> = mapOf()): HttpResponse {
            val httpBuilder: HttpUrl.Builder = url.toHttpUrlOrNull()!!.newBuilder()
            for (item in params.entries) {
                httpBuilder.addQueryParameter(item.key, item.value)
            }
            val builder = Request.Builder().url(httpBuilder.build()).post(data)
            for (item in headers.entries) {
                builder.addHeader(item.key, item.value)
            }
            val request = builder.build()
            val resp = client.newCall(request).await()
            return HttpResponse(resp.code, resp.body!!.string(), resp.headers.toMultimap())
        }
    }
}
