package com.sapuseven.untis.helpers

import com.sapuseven.untis.BuildConfig
import kotlinx.serialization.json.JSON

object SerializationUtils {
	fun getJSON(): JSON {
		return JSON(strictMode = BuildConfig.DEBUG) // Disable strict mode for release builds to prevent crashes when the API changes
	}
}