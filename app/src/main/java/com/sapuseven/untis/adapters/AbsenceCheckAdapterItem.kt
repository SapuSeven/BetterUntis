package com.sapuseven.untis.adapters

import com.sapuseven.untis.models.UntisAbsence
import com.sapuseven.untis.models.untis.response.UntisStudent

class AbsenceCheckAdapterItem(val student: UntisStudent, val absence: UntisAbsence?) {
	override fun toString(): String {
		return student.fullName()
	}
}
