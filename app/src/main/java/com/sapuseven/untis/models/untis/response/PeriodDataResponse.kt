package com.sapuseven.untis.models.untis.response

import android.os.Parcelable
import com.sapuseven.untis.api.model.untis.absence.AbsenceResult
import com.sapuseven.untis.api.model.untis.absence.StudentAbsence
import com.sapuseven.untis.api.model.untis.classreg.HomeWork
import com.sapuseven.untis.api.model.untis.timetable.LessonTopic
import com.sapuseven.untis.models.UnknownObject
import kotlinx.serialization.Serializable

@Serializable
data class PeriodDataResponse(
		val result: PeriodDataResult? = null
) : BaseResponse()

@Serializable
data class PeriodDataResult(
		val referencedStudents: List<UntisStudent>,
		val dataByTTId: Map<String, UntisPeriodData>
)

@Serializable
//@Parcelize // No idea why this is needed, but otherwise it crashes the app on pause/resume
data class UntisStudent(
		val id: Int,
		val firstName: String,
		val lastName: String
) {
	fun fullName(): String = "$firstName $lastName"
}

@Serializable
data class UntisPeriodData(
	val ttId: Int,
	val absenceChecked: Boolean,
	val studentIds: List<Int>?,
	val absences: List<StudentAbsence>?,
	val classRegEvents: List<UnknownObject>?,
	val exemptions: List<UnknownObject>?,
	val prioritizedAttendances: List<UnknownObject>?,
	val text: UnknownObject?,
	val topic: LessonTopic?,
	val homeWorks: List<HomeWork>?,
	val seatingPlan: UnknownObject?,
	val classRoles: List<UnknownObject>?,
	val channel: UnknownObject?,
	val can: List<String>
)
