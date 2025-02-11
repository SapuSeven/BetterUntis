package com.sapuseven.untis.components

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.data.database.entities.KlasseEntity
import com.sapuseven.untis.data.database.entities.RoomEntity
import com.sapuseven.untis.data.database.entities.SubjectEntity
import com.sapuseven.untis.data.database.entities.TeacherEntity
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.models.PeriodItem.Companion.ELEMENT_NAME_UNKNOWN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ElementPicker(
	private val user: User,
	private val userDao: UserDao,
) {
	private val _allClasses = MutableLiveData<Map<Long, KlasseEntity>>()
	val allClasses: LiveData<Map<Long, KlasseEntity>> by this::_allClasses

	private val _allTeachers = MutableLiveData<Map<Long, TeacherEntity>>()
	val allTeachers: LiveData<Map<Long, TeacherEntity>> by this::_allTeachers

	private val _allSubjects = MutableLiveData<Map<Long, SubjectEntity>>()
	val allSubjects: LiveData<Map<Long, SubjectEntity>> by this::_allSubjects

	private val _allRooms = MutableLiveData<Map<Long, RoomEntity>>()
	val allRooms: LiveData<Map<Long, RoomEntity>> by this::_allRooms

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

	fun getAllPeriodElements(): Map<ElementType, LiveData<List<PeriodElement>>?> = listOf(
		ElementType.CLASS,
		ElementType.TEACHER,
		ElementType.SUBJECT,
		ElementType.ROOM,
	).associateWith { getPeriodElements(it) }

	fun getShortName(id: Long, type: ElementType?): String {
		return when (type) {
			ElementType.CLASS -> _allClasses.value?.get(id)?.name
			ElementType.TEACHER -> _allTeachers.value?.get(id)?.name
			ElementType.SUBJECT -> _allSubjects.value?.get(id)?.name
			ElementType.ROOM -> _allRooms.value?.get(id)?.name
			else -> null
		} ?: ELEMENT_NAME_UNKNOWN
	}

	fun getShortName(periodElement: PeriodElement): String {
		return getShortName(periodElement.id, periodElement.type)
	}

	fun getLongName(id: Long, type: ElementType): String {
		return when (type) {
			ElementType.CLASS -> _allClasses.value?.get(id)?.longName
			ElementType.TEACHER -> _allTeachers.value?.get(id)?.run { "$firstName $lastName" }
			ElementType.SUBJECT -> _allSubjects.value?.get(id)?.longName
			ElementType.ROOM -> _allRooms.value?.get(id)?.longName
			else -> null
		} ?: ELEMENT_NAME_UNKNOWN
	}

	fun getLongName(periodElement: PeriodElement): String {
		return getLongName(periodElement.id, periodElement.type)
	}

	fun isAllowed(id: Long, type: ElementType?): Boolean {
		return true
		return when (type) {
			ElementType.CLASS -> _allClasses.value?.get(id)?.displayable
			ElementType.TEACHER -> _allTeachers.value?.get(id)?.displayAllowed
			ElementType.SUBJECT -> _allSubjects.value?.get(id)?.displayAllowed
			ElementType.ROOM -> _allRooms.value?.get(id)?.displayAllowed
			else -> null
		} ?: false
	}

	fun isAllowed(periodElement: PeriodElement): Boolean {
		return isAllowed(periodElement.id, periodElement.type)
	}

	private fun getPeriodElements(type: ElementType): LiveData<List<PeriodElement>>? = when (type) {
		ElementType.CLASS -> _allClasses.map { data -> data.values.mapToPeriodElements() }
		ElementType.TEACHER -> _allTeachers.map { data -> data.values.mapToPeriodElements() }
		ElementType.SUBJECT -> _allSubjects.map { data -> data.values.mapToPeriodElements() }
		ElementType.ROOM -> _allRooms.map { data -> data.values.mapToPeriodElements() }
		else -> null
	}

	private inline fun <reified T> Collection<T>.mapToPeriodElements(): List<PeriodElement> {
		return this.mapNotNull { item ->
			when (T::class) {
				KlasseEntity::class -> PeriodElement(ElementType.CLASS, (item as KlasseEntity).id)
				TeacherEntity::class -> PeriodElement(ElementType.TEACHER, (item as TeacherEntity).id)
				SubjectEntity::class -> PeriodElement(ElementType.SUBJECT, (item as SubjectEntity).id)
				RoomEntity::class -> PeriodElement(ElementType.ROOM, (item as RoomEntity).id)
				else -> null
			}
		}
	}
}
