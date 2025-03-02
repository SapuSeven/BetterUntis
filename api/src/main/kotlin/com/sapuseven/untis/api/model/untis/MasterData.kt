package com.sapuseven.untis.api.model.untis

import com.sapuseven.untis.api.model.untis.masterdata.AbsenceReason
import com.sapuseven.untis.api.model.untis.masterdata.Department
import com.sapuseven.untis.api.model.untis.masterdata.Duty
import com.sapuseven.untis.api.model.untis.masterdata.EventReason
import com.sapuseven.untis.api.model.untis.masterdata.EventReasonGroup
import com.sapuseven.untis.api.model.untis.masterdata.ExcuseStatus
import com.sapuseven.untis.api.model.untis.masterdata.Holiday
import com.sapuseven.untis.api.model.untis.masterdata.Klasse
import com.sapuseven.untis.api.model.untis.masterdata.Room
import com.sapuseven.untis.api.model.untis.masterdata.SchoolYear
import com.sapuseven.untis.api.model.untis.masterdata.Subject
import com.sapuseven.untis.api.model.untis.masterdata.Teacher
import com.sapuseven.untis.api.model.untis.masterdata.TeachingMethod
import com.sapuseven.untis.api.model.untis.masterdata.TimeGrid
import kotlinx.serialization.Serializable

@Serializable
data class MasterData(
	val timeStamp: Long = 0,
	val absenceReasons: List<AbsenceReason>?,
	val departments: List<Department>?,
	val duties: List<Duty>?,
	val eventReasons: List<EventReason>?,
	val eventReasonGroups: List<EventReasonGroup>?,
	val excuseStatuses: List<ExcuseStatus>?,
	val holidays: List<Holiday>?,
	val klassen: List<Klasse>,
	val rooms: List<Room>,
	val subjects: List<Subject>,
	val teachers: List<Teacher>,
	val teachingMethods: List<TeachingMethod>?,
	val schoolyears: List<SchoolYear>?,
	val timeGrid: TimeGrid?
)
