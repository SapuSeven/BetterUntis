package com.sapuseven.untis.models.untis.params

import com.sapuseven.untis.api.model.untis.Auth
import kotlinx.serialization.Serializable

@Serializable
data class DeleteAbsenceParams(
		val absenceId: Int,
		val auth: Auth
) : BaseParams()
