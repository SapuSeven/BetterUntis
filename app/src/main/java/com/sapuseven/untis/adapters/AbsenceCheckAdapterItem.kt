package com.sapuseven.untis.adapters

import com.sapuseven.untis.models.untis.response.UntisStudent
import com.sapuseven.untis.viewmodels.AbsenceCheckViewModel

class AbsenceCheckAdapterItem(
		val student: UntisStudent,
		val absence: AbsenceCheckViewModel.Absence
) {
	override fun toString(): String {
		return student.fullName()
	}
}
