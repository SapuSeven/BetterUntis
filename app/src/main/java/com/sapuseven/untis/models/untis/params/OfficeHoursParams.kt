package com.sapuseven.untis.models.untis.params

import com.sapuseven.untis.api.model.untis.Auth
import com.sapuseven.untis.models.untis.UntisDate
import kotlinx.serialization.Serializable

@Serializable
data class OfficeHoursParams(
		val klasseId: Int,
		val startDate: UntisDate,
		val auth: Auth
) : BaseParams()
