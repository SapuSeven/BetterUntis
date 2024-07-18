package com.sapuseven.untis.helpers

import com.sapuseven.untis.BuildConfig
import kotlinx.serialization.json.Json

object SerializationUtils {
	// TODO: Make sure this is a singleton and not recreated every time
	fun getJSON() = Json {
		ignoreUnknownKeys = !BuildConfig.DEBUG
		isLenient = !BuildConfig.DEBUG
		encodeDefaults = true
	}
}
