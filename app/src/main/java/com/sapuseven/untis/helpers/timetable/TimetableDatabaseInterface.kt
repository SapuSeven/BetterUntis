package com.sapuseven.untis.helpers.timetable

import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.PeriodData.Companion.ELEMENT_NAME_UNKNOWN
import com.sapuseven.untis.models.untis.masterdata.Klasse
import com.sapuseven.untis.models.untis.masterdata.Room
import com.sapuseven.untis.models.untis.masterdata.Subject
import com.sapuseven.untis.models.untis.masterdata.Teacher
import com.sapuseven.untis.models.untis.timetable.PeriodElement

class TimetableDatabaseInterface(val userDatabase: UserDatabase, val id: Long) {
	private var allClasses: Map<Int, Klasse> = mapOf()
	private var allTeachers: Map<Int, Teacher> = mapOf()
	private var allSubjects: Map<Int, Subject> = mapOf()
	private var allRooms: Map<Int, Room> = mapOf()

	enum class Type(val id: Int) {
		CLASS(1),
		TEACHER(2),
		SUBJECT(3),
		ROOM(4),
		STUDENT(5),
		LEGAL_GUARDIAN(12),
		PARENT(15),
		APPRENTICE_REPRESENTATIVE(21)
	}

	init {
		val userDao = userDatabase.userDao()
		userDao.getByIdWithData(id)?.let {
			allClasses =
				it.klasses.toList().filter { it.active }.sortedBy { it.name }.associateBy { it.id }
			allTeachers =
				it.teachers.toList().filter { it.active }.sortedBy { it.name }.associateBy { it.id }
			allSubjects =
				it.subjects.toList().filter { it.active }.sortedBy { it.name }.associateBy { it.id }
			allRooms =
				it.rooms.toList().filter { it.active }.sortedBy { it.name }.associateBy { it.id }
		}
	}

	fun getShortName(id: Int, type: Type?): String {
		return when (type) {
			Type.CLASS -> allClasses[id]?.name
			Type.TEACHER -> allTeachers[id]?.name
			Type.SUBJECT -> allSubjects[id]?.name
			Type.ROOM -> allRooms[id]?.name
			else -> null
		} ?: ELEMENT_NAME_UNKNOWN
	}

	fun getShortName(id: Int, type: String?): String {
		return getShortName(id, type?.let { Type.valueOf(it) })
	}

	fun getShortName(periodElement: PeriodElement): String {
		return getShortName(periodElement.id, periodElement.type)
	}

	fun getLongName(id: Int, type: Type): String {
		return when (type) {
			Type.CLASS -> allClasses[id]?.longName
			Type.TEACHER -> allTeachers[id]?.run { "$firstName $lastName" }
			Type.SUBJECT -> allSubjects[id]?.longName
			Type.ROOM -> allRooms[id]?.longName
			else -> null
		} ?: ELEMENT_NAME_UNKNOWN
	}

	fun getLongName(id: Int, type: String): String {
		return getLongName(id, Type.valueOf(type))
	}

	fun getLongName(periodElement: PeriodElement): String {
		return getLongName(periodElement.id, periodElement.type)
	}

	fun isAllowed(id: Int, type: Type?): Boolean {
		return when (type) {
			Type.CLASS -> allClasses[id]?.displayable
			Type.TEACHER -> allTeachers[id]?.displayAllowed
			Type.SUBJECT -> allSubjects[id]?.displayAllowed
			Type.ROOM -> allRooms[id]?.displayAllowed
			else -> null
		} ?: false
	}

	fun isAllowed(id: Int, type: String): Boolean {
		return isAllowed(id, Type.valueOf(type))
	}

	fun isAllowed(periodElement: PeriodElement): Boolean {
		return isAllowed(periodElement.id, periodElement.type)
	}

	private inline fun <reified T> convertToPeriodElement(values: Collection<T>): List<PeriodElement> {
		return values.map { item ->
			when (T::class) {
				Klasse::class -> PeriodElement(Type.CLASS.name, (item as Klasse).id)
				Teacher::class -> PeriodElement(Type.TEACHER.name, (item as Teacher).id)
				Subject::class -> PeriodElement(Type.SUBJECT.name, (item as Subject).id)
				Room::class -> PeriodElement(Type.ROOM.name, (item as Room).id)
				else -> PeriodElement("", -1, -1)
			}
		}
	}

	fun elementContains(element: PeriodElement, other: String): Boolean {
		return when (Type.valueOf(element.type)) {
			Type.CLASS -> allClasses[element.id]?.compareTo(other)
			Type.TEACHER -> allTeachers[element.id]?.compareTo(other)
			Type.SUBJECT -> allSubjects[element.id]?.compareTo(other)
			Type.ROOM -> allRooms[element.id]?.compareTo(other)
			else -> null
		} == 0
	}

	fun getElements(type: Type?): List<PeriodElement> {
		return convertToPeriodElement(
			when (type) {
				Type.CLASS -> allClasses.values
				Type.TEACHER -> allTeachers.values
				Type.SUBJECT -> allSubjects.values
				Type.ROOM -> allRooms.values
				else -> emptyList()
			}
		)
	}
}
