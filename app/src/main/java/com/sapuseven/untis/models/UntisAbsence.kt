package com.sapuseven.untis.models

import kotlinx.serialization.Serializable

@Serializable
data class UntisAbsence(
		var id: Int,
		var studentId: Int,
		var klasseId: Int,
		var startDateTime: String,
		var endDateTime: String,
		var owner: Boolean,
		var excused: Boolean,
		var excuse: UnknownObject?,
		var absenceReasonId: Int,
		var absenceReason: String,
		var text: String
)
