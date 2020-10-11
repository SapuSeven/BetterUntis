package com.sapuseven.untis.helpers.timetable

import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.PeriodData.Companion.ELEMENT_NAME_UNKNOWN
import com.sapuseven.untis.interfaces.TableModel
import com.sapuseven.untis.models.untis.masterdata.Klasse
import com.sapuseven.untis.models.untis.masterdata.Room
import com.sapuseven.untis.models.untis.masterdata.Subject
import com.sapuseven.untis.models.untis.masterdata.Teacher
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import java.io.Serializable

class TimetableDatabaseInterface(@Transient val database: UserDatabase, id: Long) : Serializable {
	private var allClasses: Map<Int, Klasse> = mapOf()
	private var allTeachers: Map<Int, Teacher> = mapOf()
	private var allSubjects: Map<Int, Subject> = mapOf()
	private var allRooms: Map<Int, Room> = mapOf()

	enum class Type {
		CLASS,
		TEACHER,
		SUBJECT,
		ROOM
	}

	init {
		database.getAdditionalUserData<Klasse>(id, Klasse())?.let { item -> allClasses = item.toList().sortedBy { it.second.name }.toMap() }
		database.getAdditionalUserData<Teacher>(id, Teacher())?.let { item -> allTeachers = item.toList().sortedBy { it.second.name }.toMap() }
		database.getAdditionalUserData<Subject>(id, Subject())?.let { item -> allSubjects = item.toList().sortedBy { it.second.name }.toMap() }
		database.getAdditionalUserData<Room>(id, Room())?.let { item -> allRooms = item.toList().sortedBy { it.second.name }.toMap() }
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

	fun getLongName(id: Int, type: Type): String {
		return when (type) {
			Type.CLASS -> allClasses[id]?.longName
			Type.TEACHER -> allTeachers[id]?.firstName + " " + allTeachers[id]?.lastName
			Type.SUBJECT -> allSubjects[id]?.longName
			Type.ROOM -> allRooms[id]?.longName
		} ?: ""
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

	private fun tableModelToPeriodElement(values: Collection<TableModel>): List<PeriodElement> {
		return values.map { item: TableModel ->
			when (item) {
				is Klasse -> PeriodElement(Type.CLASS.name, item.id, item.id)
				is Teacher -> PeriodElement(Type.TEACHER.name, item.id, item.id)
				is Subject -> PeriodElement(Type.SUBJECT.name, item.id, item.id)
				is Room -> PeriodElement(Type.ROOM.name, item.id, item.id)
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
		} == 0
	}

	fun getElements(type: Type?): List<PeriodElement> {
		return tableModelToPeriodElement(when (type) {
			Type.CLASS -> allClasses.values
			Type.TEACHER -> allTeachers.values
			Type.SUBJECT -> allSubjects.values
			Type.ROOM -> allRooms.values
			else -> emptyList()
		})
	}
}