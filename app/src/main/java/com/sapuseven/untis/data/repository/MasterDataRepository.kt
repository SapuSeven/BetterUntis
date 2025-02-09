package com.sapuseven.untis.data.repository

import android.util.Log
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
import com.sapuseven.untis.scope.UserScopeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface MasterDataRepository {
	val currentUser: User?
	val currentUserData: UserWithData?

	fun getShortName(id: Long, type: ElementType?): String

	fun getShortName(periodElement: PeriodElement): String

	fun getLongName(id: Long, type: ElementType): String

	fun getLongName(periodElement: PeriodElement): String

	fun isAllowed(id: Long, type: ElementType?): Boolean

	fun isAllowed(periodElement: PeriodElement): Boolean
}

class DefaultMasterDataRepository : MasterDataRepository {
	override val currentUser: User? = null
	override val currentUserData: UserWithData? = null

	override fun getShortName(id: Long, type: ElementType?): String = "$type:$id"

	override fun getShortName(periodElement: PeriodElement) = getShortName(periodElement.id, periodElement.type)

	override fun getLongName(id: Long, type: ElementType): String = "$type:$id"

	override fun getLongName(periodElement: PeriodElement) = getLongName(periodElement.id, periodElement.type)

	override fun isAllowed(id: Long, type: ElementType?): Boolean = true

	override fun isAllowed(periodElement: PeriodElement): Boolean = true
}

class UntisMasterDataRepository @Inject constructor(
	private val userDao: UserDao,
	private val userScopeManager: UserScopeManager,
) : MasterDataRepository {
	override val currentUser: User? = userScopeManager.userOptional

	private var _currentUserData = MutableStateFlow<UserWithData?>(null)
	override val currentUserData: UserWithData?
		get() = _currentUserData.value

	private val allClasses = MutableStateFlow<Map<Long, KlasseEntity>>(emptyMap())
	private val allTeachers = MutableStateFlow<Map<Long, TeacherEntity>>(emptyMap())
	private val allSubjects = MutableStateFlow<Map<Long, SubjectEntity>>(emptyMap())
	private val allRooms = MutableStateFlow<Map<Long, RoomEntity>>(emptyMap())

	init {
		Log.d("Performance", "MasterDataRepository init start")
		CoroutineScope(Dispatchers.IO).launch {
			userDao.getByIdWithDataFlow(userScopeManager.user.id)
				.collect { userData ->
					_currentUserData.value = userData
					allClasses.value = userData?.let { prepareElements(it.klassen) } ?: emptyMap()
					allTeachers.value = userData?.let { prepareElements(it.teachers) } ?: emptyMap()
					allSubjects.value = userData?.let { prepareElements(it.subjects) } ?: emptyMap()
					allRooms.value = userData?.let { prepareElements(it.rooms) } ?: emptyMap()
				}
		}

		// May need to implement this if there's any issues with missing data on app start
		// If performance becomes an issue, consider implementing Dagger Producers or similar asynchronous dependency initialization
		/*measureTime {
			//wait for first
		}.let {
			Log.d("Performance", "MasterDataRepository init took $it")
		}*/
	}

	private fun <T : ElementEntity> prepareElements(elements: List<T>) =
		elements.filter { it.active }.sortedBy { it.name }.associateBy { it.id }

	override fun getShortName(id: Long, type: ElementType?): String {
		return when (type) {
			ElementType.CLASS -> allClasses.value[id]?.name
			ElementType.TEACHER -> allTeachers.value[id]?.name
			ElementType.SUBJECT -> allSubjects.value[id]?.name
			ElementType.ROOM -> allRooms.value[id]?.name
			else -> null
		} ?: ELEMENT_NAME_UNKNOWN
	}

	override fun getShortName(periodElement: PeriodElement) = getShortName(periodElement.id, periodElement.type)

	override fun getLongName(id: Long, type: ElementType): String {
		return when (type) {
			ElementType.CLASS -> allClasses.value[id]?.longName
			ElementType.TEACHER -> allTeachers.value[id]?.run { "$firstName $lastName" }
			ElementType.SUBJECT -> allSubjects.value[id]?.longName
			ElementType.ROOM -> allRooms.value[id]?.longName
			else -> null
		} ?: ELEMENT_NAME_UNKNOWN
	}

	override fun getLongName(periodElement: PeriodElement) = getLongName(periodElement.id, periodElement.type)

	override fun isAllowed(id: Long, type: ElementType?): Boolean {
		return when (type) {
			ElementType.CLASS -> allClasses.value[id]?.displayable
			ElementType.TEACHER -> allTeachers.value[id]?.displayAllowed
			ElementType.SUBJECT -> allSubjects.value[id]?.displayAllowed
			ElementType.ROOM -> allRooms.value[id]?.displayAllowed
			else -> null
		} ?: false
	}

	override fun isAllowed(periodElement: PeriodElement) = isAllowed(periodElement.id, periodElement.type)
}

val LocalMasterDataRepository = compositionLocalOf<MasterDataRepository> { DefaultMasterDataRepository() }
