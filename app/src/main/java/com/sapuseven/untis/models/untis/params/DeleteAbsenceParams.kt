package com.sapuseven.untis.models.untis.params

import com.sapuseven.untis.models.untis.UntisAuth
import kotlinx.serialization.Serializable

@Serializable
data class DeleteAbsenceParams(
		val absenceId: Int,
		val auth: UntisAuth
) : BaseParams()
