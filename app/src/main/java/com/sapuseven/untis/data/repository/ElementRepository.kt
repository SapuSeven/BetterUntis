package com.sapuseven.untis.data.repository

import android.util.Log
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.data.database.entities.KlasseEntity
import com.sapuseven.untis.data.database.entities.RoomEntity
import com.sapuseven.untis.data.database.entities.SubjectEntity
import com.sapuseven.untis.data.database.entities.TeacherEntity
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.models.PeriodItem.Companion.ELEMENT_NAME_UNKNOWN
import com.sapuseven.untis.scope.UserScopeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.measureTime

@Singleton
class ElementRepository @Inject constructor(
	private val userDao: UserDao,
	private val userScopeManager: UserScopeManager,
) {
	private lateinit var allClasses: Map<Long, KlasseEntity>
	private lateinit var allTeachers: Map<Long, TeacherEntity>
	private lateinit var allSubjects: Map<Long, SubjectEntity>
	private lateinit var allRooms: Map<Long, RoomEntity>

	init {
		// If performance becomes an issue, consider implementing Dagger Producers or similar asynchronous dependency initialization
		measureTime {
			// We need to run blocking to prevent a race condition
			runBlocking(Dispatchers.IO) {
				userDao.getByIdWithData(userScopeManager.user.id)?.let {
					allClasses = it.klassen.toList().filter { it.active }.sortedBy { it.name }.associateBy { it.id }
					allTeachers = it.teachers.toList().filter { it.active }.sortedBy { it.name }.associateBy { it.id }
					allSubjects = it.subjects.toList().filter { it.active }.sortedBy { it.name }.associateBy { it.id }
					allRooms = it.rooms.toList().filter { it.active }.sortedBy { it.name }.associateBy { it.id }
				}
			}
		}.let {
			Log.d("Performance", "ElementRepository init took $it")
		}
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

	fun getShortName(periodElement: PeriodElement) = getShortName(periodElement.id, periodElement.type)

	fun getLongName(id: Long, type: ElementType): String {
		return when (type) {
			ElementType.CLASS -> allClasses[id]?.longName
			ElementType.TEACHER -> allTeachers[id]?.run { "$firstName $lastName" }
			ElementType.SUBJECT -> allSubjects[id]?.longName
			ElementType.ROOM -> allRooms[id]?.longName
			else -> null
		} ?: ELEMENT_NAME_UNKNOWN
	}

	fun getLongName(periodElement: PeriodElement) = getLongName(periodElement.id, periodElement.type)

	fun isAllowed(id: Long, type: ElementType?): Boolean {
		return when (type) {
			ElementType.CLASS -> allClasses[id]?.displayable
			ElementType.TEACHER -> allTeachers[id]?.displayAllowed
			ElementType.SUBJECT -> allSubjects[id]?.displayAllowed
			ElementType.ROOM -> allRooms[id]?.displayAllowed
			else -> null
		} ?: false
	}

	fun isAllowed(periodElement: PeriodElement) = isAllowed(periodElement.id, periodElement.type)
}
