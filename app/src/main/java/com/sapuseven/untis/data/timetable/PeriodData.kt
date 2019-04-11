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

	private fun addClass(element: PeriodElement) {
		classes.add(element)
	}

	private fun addTeacher(element: PeriodElement) {
		teachers.add(element)
	}

	private fun addSubject(element: PeriodElement) {
		subjects.add(element)
	}

	private fun addRoom(element: PeriodElement) {
		rooms.add(element)
	}

	fun setup() {
		parseElements()
	}

	fun getShortTitle(): String {
		if (subjects.size == 0)
			return ""

		val text = StringBuilder(timetableDatabaseInterface?.getShortName(subjects[0].id, TimetableDatabaseInterface.Type.SUBJECT) ?: "?")
		for (i in 1 until subjects.size)
			text.append(", ").append(timetableDatabaseInterface?.getShortName(subjects[i].id, TimetableDatabaseInterface.Type.SUBJECT))
		return text.toString()
	}

	fun getLongTitle(): String {
		if (subjects.size == 0)
			return ""

		val text = StringBuilder(timetableDatabaseInterface?.getLongName(subjects[0].id, TimetableDatabaseInterface.Type.SUBJECT) ?: "?")
		for (i in 1 until subjects.size)
			text.append(", ").append(timetableDatabaseInterface?.getLongName(subjects[i].id, TimetableDatabaseInterface.Type.SUBJECT))
		return text.toString()
	}

	fun getShortTeachers(): String {
		if (teachers.size == 0)
			return ""

		val text = StringBuilder(timetableDatabaseInterface?.getShortName(teachers[0].id, TimetableDatabaseInterface.Type.TEACHER) ?: "?")
		for (i in 1 until teachers.size)
			text.append(", ").append(timetableDatabaseInterface?.getShortName(teachers[i].id, TimetableDatabaseInterface.Type.TEACHER))
		return text.toString()
	}

	fun getShortRooms(): String {
		if (rooms.size == 0)
			return ""

		val text = StringBuilder(timetableDatabaseInterface?.getShortName(rooms[0].id, TimetableDatabaseInterface.Type.ROOM) ?: "?")
		for (i in 1 until rooms.size)
			text.append(", ").append(timetableDatabaseInterface?.getShortName(rooms[i].id, TimetableDatabaseInterface.Type.ROOM))
		return text.toString()
	}

	fun getShortClasses(): String {
		if (classes.size == 0)
			return ""

		val text = StringBuilder(timetableDatabaseInterface?.getShortName(classes[0].id, TimetableDatabaseInterface.Type.CLASS) ?: "?")
		for (i in 1 until classes.size)
			text.append(", ").append(timetableDatabaseInterface?.getShortName(classes[i].id, TimetableDatabaseInterface.Type.CLASS))
		return text.toString()
	}

	fun isCancelled(): Boolean {
		return element.`is`.contains(Period.CODE_CANCELLED)
	}

	fun isIrregular(): Boolean {
		return element.`is`.contains(Period.CODE_IRREGULAR)
	}

	fun isExam(): Boolean {
		return element.`is`.contains(Period.CODE_EXAM)
	}

}