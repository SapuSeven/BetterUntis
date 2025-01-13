package com.sapuseven.untis.data.repository

import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.data.database.entities.KlasseEntity
import com.sapuseven.untis.data.database.entities.RoomEntity
import com.sapuseven.untis.data.database.entities.SchoolYearEntity
import com.sapuseven.untis.data.database.entities.SubjectEntity
import com.sapuseven.untis.data.database.entities.TeacherEntity
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.data.database.entities.UserWithData
import com.sapuseven.untis.models.PeriodItem.Companion.ELEMENT_NAME_UNKNOWN
import com.sapuseven.untis.scope.UserScopeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import javax.inject.Inject
import kotlin.time.measureTime

interface MasterDataRepository {
	fun getShortName(id: Long, type: ElementType?): String

	fun getShortName(periodElement: PeriodElement): String

	fun getLongName(id: Long, type: ElementType): String

	fun getLongName(periodElement: PeriodElement): String

	fun isAllowed(id: Long, type: ElementType?): Boolean

	fun isAllowed(periodElement: PeriodElement): Boolean

	fun currentSchoolYear(currentDate: LocalDate = LocalDate.now()): SchoolYearEntity?
}

class DefaultMasterDataRepository : MasterDataRepository {
	override fun getShortName(id: Long, type: ElementType?): String = "$type:$id"

	override fun getShortName(periodElement: PeriodElement) = getShortName(periodElement.id, periodElement.type)

	override fun getLongName(id: Long, type: ElementType): String = "$type:$id"

	override fun getLongName(periodElement: PeriodElement) = getLongName(periodElement.id, periodElement.type)

	override fun isAllowed(id: Long, type: ElementType?): Boolean = true

	override fun isAllowed(periodElement: PeriodElement): Boolean = true

	override fun currentSchoolYear(currentDate: LocalDate): SchoolYearEntity? = null
}

class UntisMasterDataRepository @Inject constructor(
	private val userDao: UserDao,
	private val userScopeManager: UserScopeManager,
): MasterDataRepository {
	private var userWithData: UserWithData? = null

	private val allClasses: Map<Long, KlasseEntity> by lazy {
		(userWithData?.klassen ?: emptyList()).filter { it.active }.sortedBy { it.name }.associateBy { it.id }
	}
	private val allTeachers: Map<Long, TeacherEntity> by lazy {
		(userWithData?.teachers ?: emptyList()).filter { it.active }.sortedBy { it.name }.associateBy { it.id }
	}
	private val allSubjects: Map<Long, SubjectEntity> by lazy {
		(userWithData?.subjects ?: emptyList()).filter { it.active }.sortedBy { it.name }.associateBy { it.id }
	}
	private val allRooms: Map<Long, RoomEntity> by lazy {
		(userWithData?.rooms ?: emptyList()).filter { it.active }.sortedBy { it.name }.associateBy { it.id }
	}

	init {
		// If performance becomes an issue, consider implementing Dagger Producers or similar asynchronous dependency initialization
		measureTime {
			// We need to run blocking to prevent a race condition
			runBlocking(Dispatchers.IO) {
				userWithData = userDao.getByIdWithData(userScopeManager.user.id)
			}
		}.let {
			Log.d("Performance", "MasterDataRepository init took $it")
		}
	}

	override fun getShortName(id: Long, type: ElementType?): String {
		return when (type) {
			ElementType.CLASS -> allClasses[id]?.name
			ElementType.TEACHER -> allTeachers[id]?.name
			ElementType.SUBJECT -> allSubjects[id]?.name
			ElementType.ROOM -> allRooms[id]?.name
			else -> null
		} ?: ELEMENT_NAME_UNKNOWN
	}

	override fun getShortName(periodElement: PeriodElement) = getShortName(periodElement.id, periodElement.type)

	override fun getLongName(id: Long, type: ElementType): String {
		return when (type) {
			ElementType.CLASS -> allClasses[id]?.longName
			ElementType.TEACHER -> allTeachers[id]?.run { "$firstName $lastName" }
			ElementType.SUBJECT -> allSubjects[id]?.longName
			ElementType.ROOM -> allRooms[id]?.longName
			else -> null
		} ?: ELEMENT_NAME_UNKNOWN
	}

	override fun getLongName(periodElement: PeriodElement) = getLongName(periodElement.id, periodElement.type)

	override fun isAllowed(id: Long, type: ElementType?): Boolean {
		return when (type) {
			ElementType.CLASS -> allClasses[id]?.displayable
			ElementType.TEACHER -> allTeachers[id]?.displayAllowed
			ElementType.SUBJECT -> allSubjects[id]?.displayAllowed
			ElementType.ROOM -> allRooms[id]?.displayAllowed
			else -> null
		} ?: false
	}

	override fun isAllowed(periodElement: PeriodElement) = isAllowed(periodElement.id, periodElement.type)

	override fun currentSchoolYear(currentDate: LocalDate): SchoolYearEntity? = userWithData?.schoolYears?.find {
		currentDate.isAfter(it.startDate) && currentDate.isBefore(it.endDate)
	}
}

val LocalMasterDataRepository = compositionLocalOf<MasterDataRepository> { DefaultMasterDataRepository() }
