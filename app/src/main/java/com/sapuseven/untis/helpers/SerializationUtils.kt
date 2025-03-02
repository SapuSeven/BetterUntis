package com.sapuseven.untis.helpers

import com.sapuseven.untis.BuildConfig
import kotlinx.serialization.json.Json

object SerializationUtils {
	@Deprecated("Replace with injected value")
	fun getJSON() = Json {
		ignoreUnknownKeys = !BuildConfig.DEBUG
		isLenient = !BuildConfig.DEBUG
		encodeDefaults = true
	}
}
