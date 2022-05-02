package com.sapuseven.untis.models.untis

import com.sapuseven.untis.models.UnknownObject
import kotlinx.serialization.Serializable

@Serializable
data class UntisError(
	val code: Int,
	val message: String? = null, // Added default value to prevent crashes
	val data: UnknownObject? = null // TODO: Change back to UntisErrorData if needed. Not using the proper object simplifies the testing API.
)

/*@Serializable
data class UntisErrorData(
		val exceptionTypeName: String? = null,
		val message: String? = null,
		val serverTime: Long? = null
)*/
