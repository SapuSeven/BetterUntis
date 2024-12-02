package com.sapuseven.untis.components

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.sapuseven.untis.api.model.untis.masterdata.Klasse
import com.sapuseven.untis.api.model.untis.masterdata.Room
import com.sapuseven.untis.api.model.untis.masterdata.Subject
import com.sapuseven.untis.api.model.untis.masterdata.Teacher
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.data.timetable.PeriodData.Companion.ELEMENT_NAME_UNKNOWN
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface.Type
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ElementPicker(
	private val user: User,
	private val userDao: UserDao,
) {
	private val _allClasses = MutableLiveData<Map<Int, Klasse>>()
	val allClasses: LiveData<Map<Int, Klasse>> by this::_allClasses

	private val _allTeachers = MutableLiveData<Map<Int, Teacher>>()
	val allTeachers: LiveData<Map<Int, Teacher>> by this::_allTeachers

	private val _allSubjects = MutableLiveData<Map<Int, Subject>>()
	val allSubjects: LiveData<Map<Int, Subject>> by this::_allSubjects

	private val _allRooms = MutableLiveData<Map<Int, Room>>()
	val allRooms: LiveData<Map<Int, Room>> by this::_allRooms

	init {
		loadElements()
	}

	fun loadElements() {
		Log.d("ElementPicker", "loading elements")
		CoroutineScope(Dispatchers.IO).launch {
			userDao.getByIdWithData(user.id)?.let {
				_allClasses.postValue(it.klassen.toList().filter { it.active }
					.sortedBy { it.name }.associateBy { it.id })
				_allTeachers.postValue(it.teachers.toList().filter { it.active }
					.sortedBy { it.name }.associateBy { it.id })
				_allSubjects.postValue(it.subjects.toList().filter { it.active }
					.sortedBy { it.name }.associateBy { it.id })
				_allRooms.postValue(it.rooms.toList().filter { it.active }
					.sortedBy { it.name }.associateBy { it.id })
			}
		}
	}

	fun getAllPeriodElements(): Map<Type, LiveData<List<PeriodElement>>?> = listOf(
		Type.CLASS,
		Type.TEACHER,
		Type.SUBJECT,
		Type.ROOM,
	).associateWith { getPeriodElements(it) }

	fun getShortName(id: Int, type: Type?): String {
		return when (type) {
			Type.CLASS -> _allClasses.value?.get(id)?.name
			Type.TEACHER -> _allTeachers.value?.get(id)?.name
			Type.SUBJECT -> _allSubjects.value?.get(id)?.name
			Type.ROOM -> _allRooms.value?.get(id)?.name
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
			Type.CLASS -> _allClasses.value?.get(id)?.longName
			Type.TEACHER -> _allTeachers.value?.get(id)?.run { "$firstName $lastName" }
			Type.SUBJECT -> _allSubjects.value?.get(id)?.longName
			Type.ROOM -> _allRooms.value?.get(id)?.longName
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
			Type.CLASS -> _allClasses.value?.get(id)?.displayable
			Type.TEACHER -> _allTeachers.value?.get(id)?.displayAllowed
			Type.SUBJECT -> _allSubjects.value?.get(id)?.displayAllowed
			Type.ROOM -> _allRooms.value?.get(id)?.displayAllowed
			else -> null
		} ?: false
	}

	fun isAllowed(id: Int, type: String): Boolean {
		return isAllowed(id, Type.valueOf(type))
	}

	fun isAllowed(periodElement: PeriodElement): Boolean {
		return isAllowed(periodElement.id, periodElement.type)
	}

	private fun getPeriodElements(type: Type): LiveData<List<PeriodElement>>? = when (type) {
		Type.CLASS -> _allClasses.map { data -> data.values.mapToPeriodElements() }
		Type.TEACHER -> _allTeachers.map { data -> data.values.mapToPeriodElements() }
		Type.SUBJECT -> _allSubjects.map { data -> data.values.mapToPeriodElements() }
		Type.ROOM -> _allRooms.map { data -> data.values.mapToPeriodElements() }
		else -> null
	}

	private inline fun <reified T> Collection<T>.mapToPeriodElements(): List<PeriodElement> {
		return this.map { item ->
			when (T::class) {
				Klasse::class -> PeriodElement(Type.CLASS.name, (item as Klasse).id)
				Teacher::class -> PeriodElement(Type.TEACHER.name, (item as Teacher).id)
				Subject::class -> PeriodElement(Type.SUBJECT.name, (item as Subject).id)
				Room::class -> PeriodElement(Type.ROOM.name, (item as Room).id)
				else -> PeriodElement("", -1, -1)
			}
		}
	}
}
