package com.sapuseven.untis.data.repository

import androidx.compose.runtime.compositionLocalOf
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.data.database.entities.ElementEntity
import com.sapuseven.untis.data.database.entities.KlasseEntity
import com.sapuseven.untis.data.database.entities.RoomEntity
import com.sapuseven.untis.data.database.entities.SubjectEntity
import com.sapuseven.untis.data.database.entities.TeacherEntity
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.data.database.entities.UserWithData
import com.sapuseven.untis.models.PeriodItem.Companion.ELEMENT_NAME_UNKNOWN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

interface MasterDataRepository {
	val user: User?
	val userData: UserWithData?

	val allClasses: StateFlow<List<PeriodElement>>
	val allTeachers: StateFlow<List<PeriodElement>>
	val allSubjects: StateFlow<List<PeriodElement>>
	val allRooms: StateFlow<List<PeriodElement>>

	fun getShortName(id: Long, type: ElementType? = null, default: String = ELEMENT_NAME_UNKNOWN): String
	fun getShortName(periodElement: PeriodElement, default: String = ELEMENT_NAME_UNKNOWN): String

	fun getLongName(id: Long, type: ElementType, default: String = ELEMENT_NAME_UNKNOWN): String
	fun getLongName(periodElement: PeriodElement, default: String = ELEMENT_NAME_UNKNOWN): String

	fun isAllowed(id: Long, type: ElementType? = null): Boolean
	fun isAllowed(periodElement: PeriodElement): Boolean
}

@Singleton
class DefaultMasterDataRepository : MasterDataRepository {
	override val user: User? = null
	override val userData: UserWithData? = null

	override val allClasses: StateFlow<List<PeriodElement>> = MutableStateFlow(emptyList())
	override val allTeachers: StateFlow<List<PeriodElement>> = MutableStateFlow(emptyList())
	override val allSubjects: StateFlow<List<PeriodElement>> = MutableStateFlow(emptyList())
	override val allRooms: StateFlow<List<PeriodElement>> = MutableStateFlow(emptyList())

	override fun getShortName(id: Long, type: ElementType?, default: String): String = "$type:$id"
	override fun getShortName(periodElement: PeriodElement, default: String) = getShortName(periodElement.id, periodElement.type, default)

	override fun getLongName(id: Long, type: ElementType, default: String): String = "$type:$id"
	override fun getLongName(periodElement: PeriodElement, default: String) = getLongName(periodElement.id, periodElement.type)

	override fun isAllowed(id: Long, type: ElementType?): Boolean = true
	override fun isAllowed(periodElement: PeriodElement): Boolean = true
}

@Singleton
class UntisMasterDataRepository @Inject constructor(
	private val userDao: UserDao,
	private val userRepository: UserRepository
) : MasterDataRepository {
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO);

	override val user: User?
		get() = (userRepository.userState.value as? UserRepository.UserState.User)?.user

	private val _userData = MutableStateFlow<UserWithData?>(null)
	override val userData: UserWithData?
		get() = _userData.value

	private val _allClasses = MutableStateFlow<Map<Long, KlasseEntity>>(emptyMap())
	override val allClasses: StateFlow<List<PeriodElement>> = _allClasses.mapToPeriodElements(scope, ElementType.CLASS)

	private val _allTeachers = MutableStateFlow<Map<Long, TeacherEntity>>(emptyMap())
	override val allTeachers: StateFlow<List<PeriodElement>> = _allTeachers.mapToPeriodElements(scope, ElementType.TEACHER)

	private val _allSubjects = MutableStateFlow<Map<Long, SubjectEntity>>(emptyMap())
	override val allSubjects: StateFlow<List<PeriodElement>> = _allSubjects.mapToPeriodElements(scope, ElementType.SUBJECT)

	private val _allRooms = MutableStateFlow<Map<Long, RoomEntity>>(emptyMap())
	override val allRooms: StateFlow<List<PeriodElement>> = _allRooms.mapToPeriodElements(scope, ElementType.ROOM)

	init {
		scope.launch {
			userRepository.userState.collectLatest { userState ->
				(userState as? UserRepository.UserState.User)?.user?.let { user ->
					userDao.getByIdWithDataFlow(user.id).collectLatest { userData ->
						_userData.value = userData
						_allClasses.value = userData?.let { prepareElements(it.klassen) } ?: emptyMap()
						_allTeachers.value = userData?.let { prepareElements(it.teachers) } ?: emptyMap()
						_allSubjects.value = userData?.let { prepareElements(it.subjects) } ?: emptyMap()
						_allRooms.value = userData?.let { prepareElements(it.rooms) } ?: emptyMap()
					}
				} ?: run {
					_userData.value = null
					_allClasses.value = emptyMap()
					_allTeachers.value = emptyMap()
					_allSubjects.value = emptyMap()
					_allRooms.value = emptyMap()
				}
			}
		}
	}

	private fun <T : ElementEntity> prepareElements(elements: List<T>) =
		elements.filter { it.active }.sortedBy { it.name }.associateBy { it.id }

	override fun getShortName(id: Long, type: ElementType?, default: String): String {
		return when (type) {
			ElementType.CLASS -> _allClasses.value[id]?.name
			ElementType.TEACHER -> _allTeachers.value[id]?.name
			ElementType.SUBJECT -> _allSubjects.value[id]?.name
			ElementType.ROOM -> _allRooms.value[id]?.name
			else -> null
		} ?: default
	}

	override fun getShortName(periodElement: PeriodElement, default: String) =
		getShortName(periodElement.id, periodElement.type, default)

	override fun getLongName(id: Long, type: ElementType, default: String): String {
		return when (type) {
			ElementType.CLASS -> _allClasses.value[id]?.longName
			ElementType.TEACHER -> _allTeachers.value[id]?.run { "$firstName $lastName" }
			ElementType.SUBJECT -> _allSubjects.value[id]?.longName
			ElementType.ROOM -> _allRooms.value[id]?.longName
			else -> null
		} ?: default
	}

	override fun getLongName(periodElement: PeriodElement, default: String) =
		getLongName(periodElement.id, periodElement.type, default)

	override fun isAllowed(id: Long, type: ElementType?): Boolean {
		return when (type) {
			ElementType.CLASS -> _allClasses.value[id]?.displayable
			ElementType.TEACHER -> _allTeachers.value[id]?.displayAllowed
			ElementType.SUBJECT -> _allSubjects.value[id]?.displayAllowed
			ElementType.ROOM -> _allRooms.value[id]?.displayAllowed
			else -> null
		} ?: false
	}

	override fun isAllowed(periodElement: PeriodElement) = isAllowed(periodElement.id, periodElement.type)
}

private fun <T : ElementEntity> StateFlow<Map<Long, T>>.mapToPeriodElements(scope: CoroutineScope, type: ElementType): StateFlow<List<PeriodElement>> {
	return this.map { it.values.map { PeriodElement(type, it.id) }}.stateIn(
		scope = scope,
		started = SharingStarted.Eagerly,
		initialValue = emptyList()
	)
}

val LocalMasterDataRepository = compositionLocalOf<MasterDataRepository> { DefaultMasterDataRepository() }
