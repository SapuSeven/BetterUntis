package com.sapuseven.untis.api.model.response

import io.ktor.client.call.body
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import org.openapitools.client.infrastructure.BodyProvider
import org.openapitools.client.infrastructure.TypedBodyProvider

sealed class UntisResult<out T : Any?> {
	open operator fun component1(): T? = null
	open operator fun component2(): Error? = null

	inline fun <X> fold(success: (T) -> X, failure: (Error) -> X): X = when (this) {
		is Success -> success(this.value)
		is Failure -> failure(this.error)
	}

	abstract fun get(): T

	class Success<out T : Any?>(val value: T) : UntisResult<T>() {
		override fun component1(): T? = value

		override fun get(): T = value

		override fun toString() = "[Success: $value]"

		override fun hashCode(): Int = value.hashCode()

		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			return other is Success<*> && value == other.value
		}
	}

	class Failure(val error: Error) : UntisResult<Nothing>() {
		override fun component2(): Error? = error
		override fun get() = throw Exception(error.message)

		override fun toString() = "[Failure: $error]"

		override fun hashCode(): Int = error.hashCode()

		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			return other is Failure && error == other.error
		}
	}

	companion object {
		// Factory methods
		fun error(e: Error) = Failure(e)

		fun <T : Any?> success(v: T) = Success(v)

		fun <T : Any?> of(value: T?, fail: (() -> Error)): UntisResult<T> =
			value?.let { success(it) } ?: error(fail())
	}
}
