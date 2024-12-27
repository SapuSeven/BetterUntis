package com.sapuseven.untis.api.model.untis.timetable

import com.sapuseven.untis.api.model.untis.absence.StudentAbsence
import com.sapuseven.untis.api.model.untis.classreg.ClassRegEvent
import com.sapuseven.untis.api.model.untis.classreg.ClassRole
import com.sapuseven.untis.api.model.untis.classreg.HomeWork
import com.sapuseven.untis.api.model.untis.classreg.PrioritizedAttendance
import com.sapuseven.untis.api.model.untis.classreg.SeatingPlan
import com.sapuseven.untis.api.model.untis.classreg.StudentExemption
import com.sapuseven.untis.api.model.untis.enumeration.PeriodRight
import com.sapuseven.untis.api.model.untis.messenger.MessengerChannel
import kotlinx.serialization.Serializable


@Serializable
data class PeriodData(
	val ttId: Long,
	val absenceChecked: Boolean,
	val studentIds: List<Long>?,
	val absences: List<StudentAbsence>?,
	val classRegEvents: List<ClassRegEvent>?,
	val exemptions: List<StudentExemption>?,
	val prioritizedAttendances: List<PrioritizedAttendance>?,
	val text: PeriodText?,
	val topic: LessonTopic?,
	val homeWorks: List<HomeWork>?,
	val seatingPlan: SeatingPlan?,
	val classRoles: List<ClassRole>?,
	val channel: MessengerChannel,
	val can: List<PeriodRight>
)
