package com.sapuseven.untis.helpers

import com.sapuseven.untis.BuildConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

object SerializationUtils {
	fun getJSON(): Json {
		return Json(configuration = JsonConfiguration.Stable.copy(
				ignoreUnknownKeys = !BuildConfig.DEBUG,
				isLenient = !BuildConfig.DEBUG,
				serializeSpecialFloatingPointValues = !BuildConfig.DEBUG
		)) // Disable strict mode for release builds to prevent crashes when the API changes
	}
}
