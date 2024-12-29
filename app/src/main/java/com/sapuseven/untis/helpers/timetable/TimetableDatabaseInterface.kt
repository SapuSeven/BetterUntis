package com.sapuseven.untis.helpers.timetable

import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.data.database.UserDatabase
import com.sapuseven.untis.data.database.entities.KlasseEntity
import com.sapuseven.untis.data.database.entities.RoomEntity
import com.sapuseven.untis.data.database.entities.SubjectEntity
import com.sapuseven.untis.data.database.entities.TeacherEntity
import com.sapuseven.untis.data.timetable.PeriodData.Companion.ELEMENT_NAME_UNKNOWN

class TimetableDatabaseInterface(val userDatabase: UserDatabase, val id: Long) {
	var allClasses: Map<Long, KlasseEntity> = mapOf()
	private var allTeachers: Map<Long, TeacherEntity> = mapOf()
	private var allSubjects: Map<Long, SubjectEntity> = mapOf()
	private var allRooms: Map<Long, RoomEntity> = mapOf()

	@Deprecated(
		message = "Use API data type instead",
		replaceWith = ReplaceWith("ElementType", "com.sapuseven.untis.api.model.untis.enumeration.ElementType")
	)
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
		/*userDao.getByIdWithData(id)?.let {
			allClasses =
				it.klassen.toList().filter { it.active }.sortedBy { it.name }.associateBy { it.id }
			allTeachers =
				it.teachers.toList().filter { it.active }.sortedBy { it.name }.associateBy { it.id }
			allSubjects =
				it.subjects.toList().filter { it.active }.sortedBy { it.name }.associateBy { it.id }
			allRooms =
				it.rooms.toList().filter { it.active }.sortedBy { it.name }.associateBy { it.id }
		}*/
	}

	fun getShortName(id: Long, type: ElementType?): String {
		return when (type) {
			ElementType.CLASS -> allClasses[id]?.name
			ElementType.TEACHER -> allTeachers[id]?.name
			ElementType.SUBJECT -> allSubjects[id]?.name
			ElementType.ROOM -> allRooms[id]?.name
			else -> null
		} ?: ELEMENT_NAME_UNKNOWN
	}

	fun getShortName(periodElement: PeriodElement): String {
		return getShortName(periodElement.id, periodElement.type)
	}

	fun getLongName(id: Long, type: ElementType): String {
		return when (type) {
			ElementType.CLASS -> allClasses[id]?.longName
			ElementType.TEACHER -> allTeachers[id]?.run { "$firstName $lastName" }
			ElementType.SUBJECT -> allSubjects[id]?.longName
			ElementType.ROOM -> allRooms[id]?.longName
			else -> null
		} ?: ELEMENT_NAME_UNKNOWN
	}

	fun getLongName(periodElement: PeriodElement): String {
		return getLongName(periodElement.id, periodElement.type)
	}

	fun isAllowed(id: Long, type: ElementType?): Boolean {
		return when (type) {
			ElementType.CLASS -> allClasses[id]?.displayable
			ElementType.TEACHER -> allTeachers[id]?.displayAllowed
			ElementType.SUBJECT -> allSubjects[id]?.displayAllowed
			ElementType.ROOM -> allRooms[id]?.displayAllowed
			else -> null
		} ?: false
	}

	fun isAllowed(periodElement: PeriodElement): Boolean {
		return isAllowed(periodElement.id, periodElement.type)
	}

	inline fun <reified T> convertToPeriodElement(values: Collection<T>): List<PeriodElement> {
		return values.mapNotNull { item ->
			when (T::class) {
				KlasseEntity::class -> PeriodElement(ElementType.CLASS, (item as KlasseEntity).id)
				TeacherEntity::class -> PeriodElement(ElementType.TEACHER, (item as TeacherEntity).id)
				SubjectEntity::class -> PeriodElement(ElementType.SUBJECT, (item as SubjectEntity).id)
				RoomEntity::class -> PeriodElement(ElementType.ROOM, (item as RoomEntity).id)
				else -> null
			}
		}
	}

	fun elementContains(element: PeriodElement, other: String): Boolean {
		return when (element.type) {
			ElementType.CLASS -> allClasses[element.id]?.compareTo(other)
			ElementType.TEACHER -> allTeachers[element.id]?.compareTo(other)
			ElementType.SUBJECT -> allSubjects[element.id]?.compareTo(other)
			ElementType.ROOM -> allRooms[element.id]?.compareTo(other)
			else -> null
		} == 0
	}

	fun getElements(type: ElementType?): List<PeriodElement> = when (type) {
		ElementType.CLASS -> convertToPeriodElement(allClasses.values)
		ElementType.TEACHER -> convertToPeriodElement(allTeachers.values)
		ElementType.SUBJECT -> convertToPeriodElement(allSubjects.values)
		ElementType.ROOM -> convertToPeriodElement(allRooms.values)
		else -> emptyList()
	}
}
