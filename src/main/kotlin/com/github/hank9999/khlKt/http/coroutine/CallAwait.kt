/*
 * Copyright (C) 2019 gildor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications copyright (C) 2022 hank9999
 * remove `public` function keyword
 * modified package
 */

package com.github.hank9999.khlKt.http.coroutine

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Suspend extension that allows suspend [Call] inside coroutine.
 *
 * [recordStack] enables track recording, so in case of exception stacktrace will contain call stacktrace, may be useful for debugging
 *      Not free! Creates exception on each request so disabled by default, but may be enabled using system properties:
 *
 *      ```
 *      System.setProperty(OKHTTP_STACK_RECORDER_PROPERTY, OKHTTP_STACK_RECORDER_ON)
 *      ```
 *      see [README.md](https://github.com/gildor/kotlin-coroutines-okhttp/blob/master/README.md#Debugging) with details about debugging using this feature
 *
 * @return Result of request or throw exception
 */
suspend fun Call.await(recordStack: Boolean = isRecordStack): Response {
    val callStack = if (recordStack) {
        IOException().apply {
            // Remove unnecessary lines from stacktrace
            // This doesn't remove await$default, but better than nothing
            stackTrace = stackTrace.copyOfRange(1, stackTrace.size)
        }
    } else {
        null
    }
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                // Don't bother with resuming the continuation if it is already cancelled.
                if (continuation.isCancelled) return
                callStack?.initCause(e)
                continuation.resumeWithException(callStack ?: e)
            }
        })

        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch (ex: Throwable) {
                //Ignore cancel exception
            }
        }
    }
}