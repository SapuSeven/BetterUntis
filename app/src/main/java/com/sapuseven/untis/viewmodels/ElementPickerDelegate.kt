package com.sapuseven.untis.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.map
import androidx.navigation.toRoute
import com.sapuseven.untis.api.model.untis.masterdata.Klasse
import com.sapuseven.untis.api.model.untis.masterdata.Room
import com.sapuseven.untis.api.model.untis.masterdata.Subject
import com.sapuseven.untis.api.model.untis.masterdata.Teacher
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.data.timetable.PeriodData.Companion.ELEMENT_NAME_UNKNOWN
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface.Type
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.ui.navigation.AppRoutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ElementPickerDelegate {
	val allClasses: LiveData<Map<Int, Klasse>>
	val allTeachers: LiveData<Map<Int, Teacher>>
	val allSubjects: LiveData<Map<Int, Subject>>
	val allRooms: LiveData<Map<Int, Room>>

	fun init(delegateScope: CoroutineScope)

	fun getAllPeriodElements(): Map<TimetableDatabaseInterface.Type, LiveData<List<PeriodElement>>?>

	fun getShortName(periodElement: PeriodElement): String
	fun isAllowed(periodElement: PeriodElement): Boolean
}

class ElementPickerDelegateImpl @Inject constructor(
	savedStateHandle: SavedStateHandle,
	private val userDao: UserDao,
) : ViewModelDelegate(), ElementPickerDelegate {
	private val userId = savedStateHandle.get<Long>("userId")!!

	override val allClasses = MutableLiveData<Map<Int, Klasse>>()
	override val allTeachers = MutableLiveData<Map<Int, Teacher>>()
	override val allSubjects = MutableLiveData<Map<Int, Subject>>()
	override val allRooms = MutableLiveData<Map<Int, Room>>()

	override fun init() {
		delegateScope.launch(Dispatchers.IO) {
			userDao.getByIdWithData(userId)?.let {
				allClasses.postValue(it.klassen.toList().filter { it.active }
					.sortedBy { it.name }.associateBy { it.id })
				allTeachers.postValue(it.teachers.toList().filter { it.active }
					.sortedBy { it.name }.associateBy { it.id })
				allSubjects.postValue(it.subjects.toList().filter { it.active }
					.sortedBy { it.name }.associateBy { it.id })
				allRooms.postValue(it.rooms.toList().filter { it.active }
					.sortedBy { it.name }.associateBy { it.id })
			}
		}
	}

	override fun getAllPeriodElements(): Map<Type, LiveData<List<PeriodElement>>?> = listOf(
		Type.CLASS,
		Type.TEACHER,
		Type.SUBJECT,
		Type.ROOM,
	).associateWith { getPeriodElements(it) }

	fun getShortName(id: Int, type: Type?): String {
		return when (type) {
			Type.CLASS -> allClasses.value?.get(id)?.name
			Type.TEACHER -> allTeachers.value?.get(id)?.name
			Type.SUBJECT -> allSubjects.value?.get(id)?.name
			Type.ROOM -> allRooms.value?.get(id)?.name
			else -> null
		} ?: ELEMENT_NAME_UNKNOWN
	}

	fun getShortName(id: Int, type: String?): String {
		return getShortName(id, type?.let { Type.valueOf(it) })
	}

	override fun getShortName(periodElement: PeriodElement): String {
		return getShortName(periodElement.id, periodElement.type)
	}

	fun getLongName(id: Int, type: Type): String {
		return when (type) {
			Type.CLASS -> allClasses.value?.get(id)?.longName
			Type.TEACHER -> allTeachers.value?.get(id)?.run { "$firstName $lastName" }
			Type.SUBJECT -> allSubjects.value?.get(id)?.longName
			Type.ROOM -> allRooms.value?.get(id)?.longName
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
			Type.CLASS -> allClasses.value?.get(id)?.displayable
			Type.TEACHER -> allTeachers.value?.get(id)?.displayAllowed
			Type.SUBJECT -> allSubjects.value?.get(id)?.displayAllowed
			Type.ROOM -> allRooms.value?.get(id)?.displayAllowed
			else -> null
		} ?: false
	}

	fun isAllowed(id: Int, type: String): Boolean {
		return isAllowed(id, Type.valueOf(type))
	}

	override fun isAllowed(periodElement: PeriodElement): Boolean {
		return isAllowed(periodElement.id, periodElement.type)
	}

	private fun getPeriodElements(type: Type): LiveData<List<PeriodElement>>? = when (type) {
		Type.CLASS -> allClasses.map { data -> data.values.mapToPeriodElements() }
		Type.TEACHER -> allTeachers.map { data -> data.values.mapToPeriodElements() }
		Type.SUBJECT -> allSubjects.map { data -> data.values.mapToPeriodElements() }
		Type.ROOM -> allRooms.map { data -> data.values.mapToPeriodElements() }
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
