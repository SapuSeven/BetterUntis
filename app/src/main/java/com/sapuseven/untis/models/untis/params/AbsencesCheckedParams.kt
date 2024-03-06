package com.sapuseven.untis.models.untis.params

import com.sapuseven.untis.api.model.untis.Auth
import kotlinx.serialization.Serializable

@Serializable
data class AbsencesCheckedParams(
		val ttIds: List<Int>,
		val auth: Auth
) : BaseParams()
