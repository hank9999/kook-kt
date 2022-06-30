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

package com.github.hank9999.kook.http.coroutine

const val OKHTTP_STACK_RECORDER_PROPERTY = "ru.gildor.coroutines.okhttp.stackrecorder"

/**
 * Debug turned on value for [DEBUG_PROPERTY_NAME]. See [newCoroutineContext][CoroutineScope.newCoroutineContext].
 */
const val OKHTTP_STACK_RECORDER_ON = "on"

/**
 * Debug turned on value for [DEBUG_PROPERTY_NAME]. See [newCoroutineContext][CoroutineScope.newCoroutineContext].
 */
const val OKHTTP_STACK_RECORDER_OFF = "off"

@JvmField
val isRecordStack = when (System.getProperty(OKHTTP_STACK_RECORDER_PROPERTY)) {
    OKHTTP_STACK_RECORDER_ON -> true
    OKHTTP_STACK_RECORDER_OFF, null, "" -> false
    else -> error("System property '$OKHTTP_STACK_RECORDER_PROPERTY' has unrecognized value '${System.getProperty(OKHTTP_STACK_RECORDER_PROPERTY)}'")
}