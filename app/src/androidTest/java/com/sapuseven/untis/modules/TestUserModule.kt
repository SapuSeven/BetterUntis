package com.sapuseven.untis.modules

import com.sapuseven.untis.api.model.untis.MasterData
import com.sapuseven.untis.api.model.untis.UserData
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.enumeration.Right
import com.sapuseven.untis.api.model.untis.masterdata.TimeGrid
import com.sapuseven.untis.api.model.untis.masterdata.timegrid.Day
import com.sapuseven.untis.data.database.entities.AbsenceReasonEntity
import com.sapuseven.untis.data.database.entities.DepartmentEntity
import com.sapuseven.untis.data.database.entities.DutyEntity
import com.sapuseven.untis.data.database.entities.EventReasonEntity
import com.sapuseven.untis.data.database.entities.EventReasonGroupEntity
import com.sapuseven.untis.data.database.entities.ExcuseStatusEntity
import com.sapuseven.untis.data.database.entities.HolidayEntity
import com.sapuseven.untis.data.database.entities.KlasseEntity
import com.sapuseven.untis.data.database.entities.RoomEntity
import com.sapuseven.untis.data.database.entities.SchoolYearEntity
import com.sapuseven.untis.data.database.entities.SubjectEntity
import com.sapuseven.untis.data.database.entities.TeacherEntity
import com.sapuseven.untis.data.database.entities.TeachingMethodEntity
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.data.database.entities.UserWithData
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.ui.pages.login.datainput.DEMO_API_URL
import com.sapuseven.untis.utils.SCREENSHOT_PROFILE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Singleton

@Module
@TestInstallIn(
	components = [SingletonComponent::class],
	replaces = [UserModule::class, UserDatabaseModule::class]
)
object FakeUserModule {
	const val MOCK_USER_ID: Long = Long.MAX_VALUE

	private val fakeUser = User(
		id = MOCK_USER_ID,
		profileName = SCREENSHOT_PROFILE_NAME,
		apiUrl = DEMO_API_URL,
		schoolId = "test",
		user = "test.user",
		key = "test.key",
		anonymous = false,
		timeGrid = timeGrid(),
		masterDataTimestamp = 0,
		userData = UserData(
			elemType = ElementType.STUDENT,
			elemId = 1,
			displayName = "Test Student",
			schoolName = "Test School",
			departmentId = 1,
			children = emptyList(),
			klassenIds = emptyList(),
			rights = listOf(Right.R_OFFICEHOURS, Right.R_MY_ABSENCES, Right.CLASSREGISTER)
		),
		settings = null,
		created = null,
		bookmarks = emptySet()
	)

	private fun timeGrid(): TimeGrid = TimeGrid(
		days = days()
	)

	private fun days(): List<Day> {
		val days = DayOfWeek.entries.take(5)

		return days.map {
			Day(
				day = it,
				units = units()
			)
		}
	}

	private fun units(): List<com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit> = listOf(
		com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
			label = "1",
			startTime = LocalTime.of(7, 45),
			endTime = LocalTime.of(8, 35)
		),
		com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
			label = "2",
			startTime = LocalTime.of(8, 40),
			endTime = LocalTime.of(9, 30)
		),
		com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
			label = "3",
			startTime = LocalTime.of(9, 45),
			endTime = LocalTime.of(10, 35)
		),
		com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
			label = "4",
			startTime = LocalTime.of(10, 40),
			endTime = LocalTime.of(11, 30)
		),
		com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
			label = "5",
			startTime = LocalTime.of(11, 35),
			endTime = LocalTime.of(12, 25)
		),
		com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
			label = "6",
			startTime = LocalTime.of(12, 30),
			endTime = LocalTime.of(13, 20)
		),
		com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
			label = "7",
			startTime = LocalTime.of(13, 25),
			endTime = LocalTime.of(14, 15)
		),
		com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
			label = "8",
			startTime = LocalTime.of(14, 20),
			endTime = LocalTime.of(15, 10)
		),
		com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
			label = "9",
			startTime = LocalTime.of(15, 20),
			endTime = LocalTime.of(16, 10)
		),
		com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
			label = "10",
			startTime = LocalTime.of(16, 15),
			endTime = LocalTime.of(17, 5)
		),
		com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
			label = "11",
			startTime = LocalTime.of(17, 10),
			endTime = LocalTime.of(18, 0)
		),
		com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
			label = "12",
			startTime = LocalTime.of(18, 0),
			endTime = LocalTime.of(18, 45)
		),
	)

	@Singleton
	@Provides
	fun provideFakeUserScopeManager() = object : UserScopeManager {
		override val user: User = fakeUser

		override val userOptional: User = user

		override fun handleUserChange(user: User) {}
	}

	@Provides
	@Singleton
	fun provideUserDao() = object : UserDao {
		private val userWithDataFlow = MutableStateFlow<UserWithData?>(null)

		override fun getAllFlow(): Flow<List<User>> = flow { emit(getAll()) }

		@Deprecated("Use getAllFlow instead")
		override fun getAll(): List<User> = listOf(fakeUser)

		override fun getById(userId: Long): User = fakeUser

		override suspend fun getByIdAsync(userId: Long): User = fakeUser

		override suspend fun getAllWithData(): List<UserWithData> = emptyList()

		override suspend fun getByIdWithData(userId: Long): UserWithData? = userWithDataFlow.value

		override fun getByIdWithDataFlow(userId: Long): Flow<UserWithData?> = userWithDataFlow

		override suspend fun insert(user: User): Long = MOCK_USER_ID

		override suspend fun insertAbsenceReasons(absenceReasons: List<AbsenceReasonEntity>) {}

		override suspend fun insertDepartments(departments: List<DepartmentEntity>) {}

		override suspend fun insertDuties(duties: List<DutyEntity>) {}

		override suspend fun insertEventReasons(eventReasons: List<EventReasonEntity>) {}

		override suspend fun insertEventReasonGroups(eventReasonGroups: List<EventReasonGroupEntity>) {}

		override suspend fun insertExcuseStatuses(excuseStatuses: List<ExcuseStatusEntity>) {}

		override suspend fun insertHolidays(holidays: List<HolidayEntity>) {}

		override suspend fun insertKlassen(klassen: List<KlasseEntity>) {}

		override suspend fun insertRooms(rooms: List<RoomEntity>) {}

		override suspend fun insertSubjects(subjects: List<SubjectEntity>) {}

		override suspend fun insertTeachers(teachers: List<TeacherEntity>) {}

		override suspend fun insertTeachingMethods(teachingMethods: List<TeachingMethodEntity>) {}

		override suspend fun insertSchoolYears(schoolYears: List<SchoolYearEntity>) {}

		override suspend fun insertMasterData(userId: Long, masterData: MasterData) {
			userWithDataFlow.emit(UserWithData(
				fakeUser,
				masterData.absenceReasons.orEmpty().map { AbsenceReasonEntity.map(it, userId) },
				masterData.departments.orEmpty().map { DepartmentEntity.map(it, userId) },
				masterData.duties.orEmpty().map { DutyEntity.map(it, userId) },
				masterData.eventReasons.orEmpty().map { EventReasonEntity.map(it, userId) },
				masterData.eventReasonGroups.orEmpty().map { EventReasonGroupEntity.map(it, userId) },
				masterData.excuseStatuses.orEmpty().map { ExcuseStatusEntity.map(it, userId) },
				masterData.holidays.orEmpty().map { HolidayEntity.map(it, userId) },
				masterData.klassen.map { KlasseEntity.map(it, userId) },
				masterData.rooms.map { RoomEntity.map(it, userId) },
				masterData.subjects.map { SubjectEntity.map(it, userId) },
				masterData.teachers.map { TeacherEntity.map(it, userId) },
				masterData.teachingMethods.orEmpty().map { TeachingMethodEntity.map(it, userId) },
				masterData.schoolyears.orEmpty().map { SchoolYearEntity.map(it, userId) },
			))
		}

		override suspend fun upsertAbsenceReasons(absenceReasons: List<AbsenceReasonEntity>) {}

		override suspend fun upsertDepartments(departments: List<DepartmentEntity>) {}

		override suspend fun upsertDuties(duties: List<DutyEntity>) {}

		override suspend fun upsertEventReasons(eventReasons: List<EventReasonEntity>) {}

		override suspend fun upsertEventReasonGroups(eventReasonGroups: List<EventReasonGroupEntity>) {}

		override suspend fun upsertExcuseStatuses(excuseStatuses: List<ExcuseStatusEntity>) {}

		override suspend fun upsertHolidays(holidays: List<HolidayEntity>) {}

		override suspend fun upsertKlassen(klassen: List<KlasseEntity>) {}

		override suspend fun upsertRooms(rooms: List<RoomEntity>) {}

		override suspend fun upsertSubjects(subjects: List<SubjectEntity>) {}

		override suspend fun upsertTeachers(teachers: List<TeacherEntity>) {}

		override suspend fun upsertTeachingMethods(teachingMethods: List<TeachingMethodEntity>) {}

		override suspend fun upsertSchoolYears(schoolYears: List<SchoolYearEntity>) {}

		override suspend fun updateMasterDataTimestamp(userId: Long, timestamp: Long) {}

		override suspend fun update(user: User) {}

		override suspend fun delete(user: User) {}

		override suspend fun deleteAbsenceReasons(absenceReasons: List<AbsenceReasonEntity>) {}

		override suspend fun deleteDepartments(departments: List<DepartmentEntity>) {}

		override suspend fun deleteDuties(duties: List<DutyEntity>) {}

		override suspend fun deleteEventReasons(eventReasons: List<EventReasonEntity>) {}

		override suspend fun deleteEventReasonGroups(eventReasonGroups: List<EventReasonGroupEntity>) {}

		override suspend fun deleteExcuseStatuses(excuseStatuses: List<ExcuseStatusEntity>) {}

		override suspend fun deleteHolidays(holidays: List<HolidayEntity>) {}

		override suspend fun deleteKlassen(klassen: List<KlasseEntity>) {}

		override suspend fun deleteRooms(rooms: List<RoomEntity>) {}

		override suspend fun deleteSubjects(subjects: List<SubjectEntity>) {}

		override suspend fun deleteTeachers(teachers: List<TeacherEntity>) {}

		override suspend fun deleteTeachingMethods(teachingMethods: List<TeachingMethodEntity>) {}

		override suspend fun deleteSchoolYears(schoolYears: List<SchoolYearEntity>) {}
	}
}
