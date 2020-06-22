package com.sapuseven.untis.models.untis.response

import kotlinx.serialization.Serializable

@Serializable
data class DeleteAbsenceResponse(
		val result: DeleteAbsenceResult? = null
) : BaseResponse()

@Serializable
data class DeleteAbsenceResult(
		val success: Boolean
)
