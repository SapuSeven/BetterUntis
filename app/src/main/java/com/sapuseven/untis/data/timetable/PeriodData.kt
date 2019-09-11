package com.sapuseven.untis.data.timetable

import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.Period
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class PeriodData(
		@Transient private var timetableDatabaseInterface: TimetableDatabaseInterface? = null, // TODO: Better error handling if this is null, for example after deserialization
		var element: Period,
		var cutOff: Boolean = false,
		var coveringElements: Boolean = false,
		var visible: Boolean = true,
		var durationInHours: Int = 0
) {
	val classes = ArrayList<PeriodElement>()
	val teachers = ArrayList<PeriodElement>()
	val subjects = ArrayList<PeriodElement>()
	val rooms = ArrayList<PeriodElement>()

	companion object {
		// TODO: Convert to string resources
		const val ELEMENT_NAME_SEPARATOR = ", "
		const val ELEMENT_NAME_UNKNOWN = "?"
	}

	private fun parseElements() {
		element.elements.forEach { element ->
			// TODO: Don't hard-code these values
			when (element.type) {
				"CLASS" -> addClass(element)
				"TEACHER" -> addTeacher(element)
				"SUBJECT" -> addSubject(element)
				"ROOM" -> addRoom(element)
			}
		}
	}

	private fun addClass(element: PeriodElement) = classes.add(element)

	private fun addTeacher(element: PeriodElement) = teachers.add(element)

	private fun addSubject(element: PeriodElement) = subjects.add(element)

	private fun addRoom(element: PeriodElement) = rooms.add(element)

	fun setup() = parseElements()

	fun getShortTitle() = subjects.joinToString(ELEMENT_NAME_SEPARATOR) {
		timetableDatabaseInterface?.getShortName(it.id, TimetableDatabaseInterface.Type.SUBJECT)
				?: ELEMENT_NAME_UNKNOWN
	}

	fun getLongTitle() = subjects.joinToString(ELEMENT_NAME_SEPARATOR) {
		timetableDatabaseInterface?.getLongName(it.id, TimetableDatabaseInterface.Type.SUBJECT)
				?: ELEMENT_NAME_UNKNOWN
	}

	fun getShortTeachers() = teachers.joinToString(ELEMENT_NAME_SEPARATOR) {
		timetableDatabaseInterface?.getShortName(it.id, TimetableDatabaseInterface.Type.TEACHER)
				?: ELEMENT_NAME_UNKNOWN
	}

	fun getLongTeachers() = teachers.joinToString(ELEMENT_NAME_SEPARATOR) {
		timetableDatabaseInterface?.getLongName(it.id, TimetableDatabaseInterface.Type.TEACHER)
				?: ELEMENT_NAME_UNKNOWN
	}

	fun getShortRooms() = rooms.joinToString(ELEMENT_NAME_SEPARATOR) {
		timetableDatabaseInterface?.getShortName(it.id, TimetableDatabaseInterface.Type.ROOM)
				?: ELEMENT_NAME_UNKNOWN
	}

	fun getLongRooms() = rooms.joinToString(ELEMENT_NAME_SEPARATOR) {
		timetableDatabaseInterface?.getLongName(it.id, TimetableDatabaseInterface.Type.ROOM)
				?: ELEMENT_NAME_UNKNOWN
	}

	fun getShortClasses() = classes.joinToString(ELEMENT_NAME_SEPARATOR) {
		timetableDatabaseInterface?.getShortName(it.id, TimetableDatabaseInterface.Type.CLASS)
				?: ELEMENT_NAME_UNKNOWN
	}

	fun getLongClasses() = classes.joinToString(ELEMENT_NAME_SEPARATOR) {
		timetableDatabaseInterface?.getLongName(it.id, TimetableDatabaseInterface.Type.CLASS)
				?: ELEMENT_NAME_UNKNOWN
	}

	fun isCancelled(): Boolean = element.`is`.contains(Period.CODE_CANCELLED)

	fun isIrregular(): Boolean = element.`is`.contains(Period.CODE_IRREGULAR)

	fun isExam(): Boolean = element.`is`.contains(Period.CODE_EXAM)
}