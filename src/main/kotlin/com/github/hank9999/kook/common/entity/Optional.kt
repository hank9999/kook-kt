/*     MIT License

     Copyright (c) 2019 Hope

     Permission is hereby granted, free of charge, to any person obtaining a copy
     of this software and associated documentation files (the "Software"), to deal
     in the Software without restriction, including without limitation the rights
     to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
     copies of the Software, and to permit persons to whom the Software is
     furnished to do so, subject to the following conditions:

     The above copyright notice and this permission notice shall be included in all
     copies or substantial portions of the Software.

     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
     SOFTWARE.

     This file was copied from the kordlib/kord project under MIT License.
     Original source: https://github.com/kordlib/kord/blob/main/common/src/commonMain/kotlin/entity/optional/Optional.kt
     Copyright belongs to the original project.
     Modified by hank9999 in 2025.
     Changes: 1. Remove some feature for simplification.
              2. Translation.
 */

package com.github.hank9999.kook.common.entity

import com.github.hank9999.kook.common.entity.Optional.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 *表示一个可能处于三种状态的值。*
 *具体来说:*
 * * [Missing] - 在序列化实体中不存在的字段
 * * [Null] - 在序列化实体中被赋值为null的字段
 * * [Value] - 在序列化实体中被赋值为非null值的字段
 *
 * 可以通过kotlinx.serialization进行(反)序列化, 比如：
 * * `Optional<T>` - 只是可选但不可为null的字段
 * * `Optional<T?>` - 既可选又可为null的字段
 * * 只是可为null的字段应该用`T?`表示
 *
 * 尝试将`null`反序列化为`Optional<T>`将导致抛出[SerializationException]异常。
 *
 * 注意，`Optional`字段应该有一个默认值`Optional.Missing`：
 * ```kotlin
 * @Serializable
 * class User(
 *     val id: String
 *     val isBot: Optional<Boolean?> = Optional.Missing()
 * )
 * ```
 */
@Serializable(with = OptionalSerializer::class)
public sealed class Optional<out T> {

    /**
     * 这个Optional的值。
     * * [Missing]和[Null]总是返回`null`。
     * * [Value]总是返回一个非null值。
     */
    public open val value: T?
        get() = throw UnsupportedOperationException("This is implemented in implementation classes")

    /**
     * 表示在序列化实体中不存在的字段。
     */
    public class Missing<out T> private constructor() : Optional<T>() {
        /**
         * 这个Optional的值总为`null`。
         */
        override val value: T?
            get() = null

        override fun toString(): String = "Optional.Missing"

        override fun equals(other: Any?): Boolean {
            return other is Missing<*>
        }

        override fun hashCode(): Int = 0

        public companion object {
            private val constantNull = Missing<Nothing>()

            public operator fun <T : Any> invoke(): Missing<T> = constantNull
        }
    }

    /**
     * 表示在序列化实体中被赋值为null的字段。
     */
    public class Null<out T> private constructor() : Optional<T?>() {
        /**
         * 这个Optional的值总为`null`。
         */
        override val value: T?
            get() = null

        override fun toString(): String = "Optional.Null"

        override fun equals(other: Any?): Boolean {
            return other is Null<*>
        }

        override fun hashCode(): Int = 0

        public companion object {
            private val constantNull = Null<Nothing>()

            public operator fun <T : Any> invoke(): Null<T> = constantNull
        }
    }

    /**
     * 表示在序列化实体中被赋值为非null值的字段。
     * 比较和哈希通过其[value]实现。
     *
     * @param value 这个Optional包装的值。
     */
    public class Value<T : Any>(override val value: T) : Optional<T>() {
        override fun toString(): String = "Optional.Something(content=$value)"

        /**
         * 将这个Optional解构为其[value]。
         */
        public operator fun component1(): T = value

        override fun equals(other: Any?): Boolean {
            val value = other as? Value<*> ?: return false
            return value.value == this.value
        }

        override fun hashCode(): Int = value.hashCode()
    }

    public companion object {

        public fun <T, C : Collection<T>> missingOnEmpty(value: C): Optional<C> =
            if (value.isEmpty()) Missing()
            else Value(value)

        /**
         * 返回类型为[T]的[Missing] Optional。
         */
        public operator fun <T : Any> invoke(): Missing<T> = Missing()

        /**
         * 返回类型为[T]的带有给定[value]的[Value] Optional。
         */
        public operator fun <T : Any> invoke(value: T): Value<T> = Value(value)

        /**
         * 返回一个[Optional]，当[value]非null时为[value]，当为`null`时为[Null]。
         */
        @JvmName("invokeNullable")
        public operator fun <T : Any> invoke(value: T?): Optional<T?> = when (value) {
            null -> Null()
            else -> Value(value)
        }
    }

    internal class OptionalSerializer<T>(private val contentSerializer: KSerializer<T>) : KSerializer<Optional<T>> {
        override val descriptor: SerialDescriptor = contentSerializer.descriptor

        @OptIn(ExperimentalSerializationApi::class)
        override fun deserialize(decoder: Decoder): Optional<T> {
            /**
             * 一个Optional不能既是<T: Any>又表示为可空。
             */
            if (!descriptor.isNullable && !decoder.decodeNotNullMark()) {
                throw SerializationException("descriptor for ${descriptor.serialName} was not nullable but null mark was encountered")
            }

            /**
             * 无法想到一种方法让编译器相信<T>不是可空的，
             * 即使已经亲自证明了上面的序列化器不能返回null，
             * 所以假装知道在做什么。
             */
            val optional: Optional<T?> = when {
                !decoder.decodeNotNullMark() -> {
                    decoder.decodeNull()
                    Null<Nothing>()
                }
                else -> Optional(decoder.decodeSerializableValue(contentSerializer))
            }

            @Suppress("UNCHECKED_CAST")
            return optional as Optional<T>
        }

        @OptIn(ExperimentalSerializationApi::class)
        override fun serialize(encoder: Encoder, value: Optional<T>) = when (value) {
            is Missing<*> -> throw SerializationException("missing values cannot be serialized")
            is Null<*> -> encoder.encodeNull()
            is Value -> encoder.encodeSerializableValue(contentSerializer, value.value)
        }
    }
}

public fun <T : Any> Optional<T>.switchOnMissing(value: T): Optional<T> = when (this) {
    is Missing -> Value(value)
    is Null<*>, is Value -> this
}

public fun <T : Any> Optional<T>.switchOnMissing(value: Optional<T>): Optional<T> = when (this) {
    is Missing -> value
    is Null<*>, is Value -> this
}

public fun <E> Optional<List<E>>.orEmpty(): List<E> = when (this) {
    is Missing, is Null<*> -> emptyList()
    is Value -> value
}

public fun <E> Optional<Set<E>>.orEmpty(): Set<E> = when (this) {
    is Missing, is Null<*> -> emptySet()
    is Value -> value
}

@Suppress("UNCHECKED_CAST")
public inline fun <E, T> Optional<List<E>>.mapList(mapper: (E) -> T): Optional<List<T>> = when (this) {
    is Missing, is Null<*> -> this as Optional<List<T>>
    is Value -> Value(value.map(mapper))
}

@JvmName("mapNullableList")
public inline fun <E, T> Optional<List<E>?>.mapList(mapper: (E) -> T): Optional<List<T>?> = when (this) {
    is Missing -> Missing()
    is Null -> Null()
    is Value -> Value(value!!.map(mapper))
}

public fun <T> Optional<MutableList<T>>.mapCopy(): Optional<List<T>> = map { mutable -> mutable.toList() }

@JvmName("mapCopyOfMap")
public fun <K, V> Optional<MutableMap<K, V>>.mapCopy(): Optional<Map<K, V>> = map { mutable -> mutable.toMap() }


@Suppress("UNCHECKED_CAST")
public inline fun <K, V, R> Optional<Map<K, V>>.mapValues(mapper: (Map.Entry<K, V>) -> R): Optional<Map<K, R>> = when (this) {
    is Missing, is Null<*> -> this as Optional<Map<K, R>>
    is Value -> Value(value.mapValues(mapper))
}


public inline fun <E> Optional<List<E>>.filterList(mapper: (E) -> Boolean): Optional<List<E>> = when (this) {
    is Missing, is Null<*> -> this
    is Value -> Value(value.filter(mapper))
}

@Suppress("UNCHECKED_CAST")
public inline fun <reified R> Optional<List<*>>.filterInstanceOfList(): Optional<List<R>> = when (this) {
    is Missing, is Null<*> -> this as Optional<List<R>>
    is Value -> Value(value.filterIsInstance<R>())
}


@Suppress("UNCHECKED_CAST")
public inline fun <E : Any, T : Any> Optional<E>.map(mapper: (E) -> T): Optional<T> = when (this) {
    is Missing, is Null<*> -> this as Optional<T>
    is Value -> Value(mapper(value))
}

/**
 * 如果是[Value]，则对Optional应用[mapper]，否则返回相同的Optional。
 */
@Suppress("UNCHECKED_CAST")
public inline fun <E : Any, T : Any> Optional<E>.flatMap(mapper: (E) -> Optional<T>): Optional<T> = when (this) {
    is Missing, is Null<*> -> this as Optional<T>
    is Value -> mapper(value)
}

@Suppress("UNCHECKED_CAST")
@JvmName("mapNullableOptional")
public inline fun <E : Any, T : Any> Optional<E?>.map(mapper: (E) -> T): Optional<T?> = when (this) {
    is Missing, is Null<*> -> this as Optional<T>
    is Value -> Value(mapper(value!!))
}

@Suppress("UNCHECKED_CAST")
public inline fun <E, T> Optional<E>.mapNullable(mapper: (E) -> T): Optional<T?> = when (this) {
    is Missing, is Null<*> -> this as Optional<T>
    is Value -> Optional(mapper(value))
}

@Suppress("UNCHECKED_CAST")
public inline fun <E : Any, T> Optional<E?>.mapNotNull(mapper: (E) -> T): Optional<T?> = when (this) {
    is Missing -> this as Optional<T?>
    is Null<*> -> this as Optional<T?>
    is Value -> Optional(mapper(value!!))
}

public inline fun <E> Optional<List<E>>.firstOrNull(predicate: (E) -> Boolean): E? = when (this) {
    is Missing, is Null<*> -> null
    is Value -> value.firstOrNull(predicate)
}


public inline fun <E> Optional<List<E>>.first(predicate: (E) -> Boolean): E = firstOrNull(predicate)!!


public inline fun <T, R : Any> Optional<T>.unwrap(mapper: (T) -> R): R? = when (this) {
    is Missing, is Null<*> -> null
    is Value -> mapper(value)
}

@Suppress("UNCHECKED_CAST")
public fun <T : Any> Optional<T?>.coerceToMissing(): Optional<T> = when (this) {
    is Missing, is Null -> Missing()
    is Value -> this as Value<T>
}

@Suppress("RemoveRedundantQualifierName")
public fun <T : Any> T.optional(): Optional.Value<T> = Optional.Value(this)

public fun <T : Any?> T?.optional(): Optional<T?> = Optional(this)