package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.models.UnknownObject
import kotlinx.serialization.Serializable

@Serializable
data class AbsencesCheckedResponse(
		val result: UnknownObject? = null
) : BaseResponse()
