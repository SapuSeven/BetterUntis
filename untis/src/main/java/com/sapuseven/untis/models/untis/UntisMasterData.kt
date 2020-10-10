package com.sapuseven.untis.models.untis

import com.sapuseven.untis.models.untis.masterdata.*
import kotlinx.serialization.Serializable

@Serializable
data class UntisMasterData(
		val timeStamp: Long = 0,
		val absenceReasons: List<AbsenceReason>,
		val departments: List<Department>,
		val duties: List<Duty>,
		val eventReasons: List<EventReason>,
		val eventReasonGroups: List<EventReasonGroup>,
		val excuseStatuses: List<ExcuseStatus>,
		val holidays: List<Holiday>,
		val klassen: List<Klasse>,
		val rooms: List<Room>,
		val subjects: List<Subject>,
		val teachers: List<Teacher>,
		val teachingMethods: List<TeachingMethod>,
		val schoolyears: List<SchoolYear>,
		val timeGrid: TimeGrid?
)
