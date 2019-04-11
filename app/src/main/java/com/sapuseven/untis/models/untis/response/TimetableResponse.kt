package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.models.UntisTimetable
import com.sapuseven.untis.models.UntisUserData
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class TimetableResponse(
		@Optional val result: UntisTimetable? = null
) : BaseResponse()