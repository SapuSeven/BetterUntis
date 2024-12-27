package com.sapuseven.untis.data.database.entities

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.MasterData
import com.sapuseven.untis.api.model.untis.Settings
import com.sapuseven.untis.api.model.untis.UserData
import com.sapuseven.untis.api.model.untis.masterdata.TimeGrid
import com.sapuseven.untis.models.TimetableBookmark
import kotlinx.coroutines.flow.Flow

@Entity
data class User(
	@PrimaryKey(autoGenerate = true) val id: Long,
	val profileName: String = "",
	val apiUrl: String,
	val schoolId: String,
	val user: String? = null,
	val key: String? = null,
	val anonymous: Boolean = false,
	val timeGrid: TimeGrid,
	val masterDataTimestamp: Long,
	val userData: UserData,
	val settings: Settings? = null,
	val created: Long? = null,
	var bookmarks: Set<TimetableBookmark>
) {
	fun getDisplayedName(context: Context): String {
		return when {
			profileName.isNotBlank() -> profileName
			anonymous -> context.getString(R.string.all_anonymous)
			else -> userData.displayName
		}
	}

	@Composable
	fun getDisplayedName(): String {
		return when {
			profileName.isNotBlank() -> profileName
			anonymous -> stringResource(R.string.all_anonymous)
			else -> userData.displayName
		}
	}
}

data class UserWithData(
	@Embedded val user: User,

	@Relation(parentColumn = "id", entityColumn = "userId") val absenceReasons: List<AbsenceReasonEntity>,

	@Relation(parentColumn = "id", entityColumn = "userId") val departments: List<DepartmentEntity>,

	@Relation(parentColumn = "id", entityColumn = "userId") val duties: List<DutyEntity>,

	@Relation(parentColumn = "id", entityColumn = "userId") val eventReasons: List<EventReasonEntity>,

	@Relation(parentColumn = "id", entityColumn = "userId") val eventReasonGroups: List<EventReasonGroupEntity>,

	@Relation(parentColumn = "id", entityColumn = "userId") val excuseStatuses: List<ExcuseStatusEntity>,

	@Relation(parentColumn = "id", entityColumn = "userId") val holidays: List<HolidayEntity>,

	@Relation(parentColumn = "id", entityColumn = "userId") val klassen: List<KlasseEntity>,

	@Relation(parentColumn = "id", entityColumn = "userId") val rooms: List<RoomEntity>,

	@Relation(parentColumn = "id", entityColumn = "userId") val subjects: List<SubjectEntity>,

	@Relation(parentColumn = "id", entityColumn = "userId") val teachers: List<TeacherEntity>,

	@Relation(parentColumn = "id", entityColumn = "userId") val teachingMethods: List<TeachingMethodEntity>,

	@Relation(parentColumn = "id", entityColumn = "userId") val schoolYears: List<SchoolYearEntity>,
)

@Dao
interface UserDao {
	@Query("SELECT * FROM user")
	fun getAllFlow(): Flow<List<User>>

	@Deprecated("Should be migrated to getAllFlow()")
	@Query("SELECT * FROM user")
	fun getAll(): List<User>

	@Deprecated("Should be migrated to getAllFlow()")
	@Query("SELECT * FROM user")
	suspend fun getAllAsync(): List<User>

	@Query("SELECT * FROM user WHERE id LIKE :userId")
	fun getById(userId: Long): User?

	@Query("SELECT * FROM user WHERE id LIKE :userId")
	suspend fun getByIdAsync(userId: Long): User?

	@Transaction
	@Query("SELECT * FROM user")
	fun getAllWithData(): List<UserWithData>

	@Transaction
	@Query("SELECT * FROM user WHERE id LIKE :userId")
	fun getByIdWithData(userId: Long): UserWithData?

	@Insert
	fun insert(user: User): Long

	@Insert
	fun insertAbsenceReasons(absenceReasons: List<AbsenceReasonEntity>)

	@Insert
	fun insertDepartments(departments: List<DepartmentEntity>)

	@Insert
	fun insertDuties(duties: List<DutyEntity>)

	@Insert
	fun insertEventReasons(eventReasons: List<EventReasonEntity>)

	@Insert
	fun insertEventReasonGroups(eventReasonGroups: List<EventReasonGroupEntity>)

	@Insert
	fun insertExcuseStatuses(excuseStatuses: List<ExcuseStatusEntity>)

	@Insert
	fun insertHolidays(holidays: List<HolidayEntity>)

	@Insert
	fun insertKlassen(klassen: List<KlasseEntity>)

	@Insert
	fun insertRooms(rooms: List<RoomEntity>)

	@Insert
	fun insertSubjects(subjects: List<SubjectEntity>)

	@Insert
	fun insertTeachers(teachers: List<TeacherEntity>)

	@Insert
	fun insertTeachingMethods(teachingMethods: List<TeachingMethodEntity>)

	@Insert
	fun insertSchoolYears(schoolYears: List<SchoolYearEntity>)

	@Transaction
	fun insertUserData(userId: Long, masterData: MasterData) {
		insertAbsenceReasons(masterData.absenceReasons.orEmpty().map { AbsenceReasonEntity.map(it, userId) })
		insertDepartments(masterData.departments.orEmpty().map { DepartmentEntity.map(it, userId) })
		insertDuties(masterData.duties.orEmpty().map { DutyEntity.map(it, userId) })
		insertEventReasons(masterData.eventReasons.orEmpty().map { EventReasonEntity.map(it, userId) })
		insertEventReasonGroups(masterData.eventReasonGroups.orEmpty().map { EventReasonGroupEntity.map(it, userId) })
		insertExcuseStatuses(masterData.excuseStatuses.orEmpty().map { ExcuseStatusEntity.map(it, userId) })
		insertHolidays(masterData.holidays.orEmpty().map { HolidayEntity.map(it, userId) })
		insertKlassen(masterData.klassen.map { KlasseEntity.map(it, userId) })
		insertRooms(masterData.rooms.map { RoomEntity.map(it, userId) })
		insertSubjects(masterData.subjects.map { SubjectEntity.map(it, userId) })
		insertTeachers(masterData.teachers.map { TeacherEntity.map(it, userId) })
		insertTeachingMethods(masterData.teachingMethods.orEmpty().map { TeachingMethodEntity.map(it, userId) })
		insertSchoolYears(masterData.schoolyears.orEmpty().map { SchoolYearEntity.map(it, userId) })
	}

	@Update
	fun update(user: User)

	@Delete
	fun delete(user: User)

	@Delete
	fun deleteAbsenceReasons(absenceReasons: List<AbsenceReasonEntity>)

	@Delete
	fun deleteDepartments(departments: List<DepartmentEntity>)

	@Delete
	fun deleteDuties(duties: List<DutyEntity>)

	@Delete
	fun deleteEventReasons(eventReasons: List<EventReasonEntity>)

	@Delete
	fun deleteEventReasonGroups(eventReasonGroups: List<EventReasonGroupEntity>)

	@Delete
	fun deleteExcuseStatuses(excuseStatuses: List<ExcuseStatusEntity>)

	@Delete
	fun deleteHolidays(holidays: List<HolidayEntity>)

	@Delete
	fun deleteKlassen(klassen: List<KlasseEntity>)

	@Delete
	fun deleteRooms(rooms: List<RoomEntity>)

	@Delete
	fun deleteSubjects(subjects: List<SubjectEntity>)

	@Delete
	fun deleteTeachers(teachers: List<TeacherEntity>)

	@Delete
	fun deleteTeachingMethods(teachingMethods: List<TeachingMethodEntity>)

	@Delete
	fun deleteSchoolYears(schoolYears: List<SchoolYearEntity>)

	@Transaction
	fun deleteUserData(userWithData: UserWithData) {
		deleteAbsenceReasons(userWithData.absenceReasons)
		deleteDepartments(userWithData.departments)
		deleteDuties(userWithData.duties)
		deleteEventReasons(userWithData.eventReasons)
		deleteEventReasonGroups(userWithData.eventReasonGroups)
		deleteExcuseStatuses(userWithData.excuseStatuses)
		deleteHolidays(userWithData.holidays)
		deleteKlassen(userWithData.klassen)
		deleteRooms(userWithData.rooms)
		deleteSubjects(userWithData.subjects)
		deleteTeachers(userWithData.teachers)
		deleteTeachingMethods(userWithData.teachingMethods)
		deleteSchoolYears(userWithData.schoolYears)
	}

	@Transaction
	fun deleteUserData(userId: Long) = getByIdWithData(userId)?.let { deleteUserData(it) }
}
