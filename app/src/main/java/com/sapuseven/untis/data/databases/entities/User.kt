package com.sapuseven.untis.data.databases.entities

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.room.*
import com.sapuseven.untis.R
import com.sapuseven.untis.models.TimetableBookmark
import com.sapuseven.untis.models.untis.UntisMasterData
import com.sapuseven.untis.models.untis.UntisSettings
import com.sapuseven.untis.models.untis.UntisUserData
import com.sapuseven.untis.models.untis.masterdata.*
import com.sapuseven.untis.models.untis.masterdata.Room

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
	val userData: UntisUserData,
	val settings: UntisSettings? = null,
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

	@Relation(parentColumn = "id", entityColumn = "userId")
	val absenceReasons: List<AbsenceReason>,

	@Relation(parentColumn = "id", entityColumn = "userId")
	val departments: List<Department>,

	@Relation(parentColumn = "id", entityColumn = "userId")
	val duties: List<Duty>,

	@Relation(parentColumn = "id", entityColumn = "userId")
	val eventReasons: List<EventReason>,

	@Relation(parentColumn = "id", entityColumn = "userId")
	val eventReasonGroups: List<EventReasonGroup>,

	@Relation(parentColumn = "id", entityColumn = "userId")
	val excuseStatuses: List<ExcuseStatus>,

	@Relation(parentColumn = "id", entityColumn = "userId")
	val holidays: List<Holiday>,

	@Relation(parentColumn = "id", entityColumn = "userId")
	val klassen: List<Klasse>,

	@Relation(parentColumn = "id", entityColumn = "userId")
	val rooms: List<Room>,

	@Relation(parentColumn = "id", entityColumn = "userId")
	val subjects: List<Subject>,

	@Relation(parentColumn = "id", entityColumn = "userId")
	val teachers: List<Teacher>,

	@Relation(parentColumn = "id", entityColumn = "userId")
	val teachingMethods: List<TeachingMethod>,

	@Relation(parentColumn = "id", entityColumn = "userId")
	val schoolYears: List<SchoolYear>,
)

@Dao
interface UserDao {
	@Query("SELECT * FROM user")
	fun getAll(): List<User>

	@Query("SELECT * FROM user WHERE id LIKE :userId")
	fun getById(userId: Long): User?

	@Transaction
	@Query("SELECT * FROM user")
	fun getAllWithData(): List<UserWithData>

	@Transaction
	@Query("SELECT * FROM user WHERE id LIKE :userId")
	fun getByIdWithData(userId: Long): UserWithData?

	@Insert
	fun insert(user: User): Long

	@Insert fun insertAbsenceReasons(absenceReasons: List<AbsenceReason>)
	@Insert fun insertDepartments(departments: List<Department>)
	@Insert fun insertDuties(duties: List<Duty>)
	@Insert fun insertEventReasons(eventReasons: List<EventReason>)
	@Insert fun insertEventReasonGroups(eventReasonGroups: List<EventReasonGroup>)
	@Insert fun insertExcuseStatuses(excuseStatuses: List<ExcuseStatus>)
	@Insert fun insertHolidays(holidays: List<Holiday>)
	@Insert fun insertKlassen(klassen: List<Klasse>)
	@Insert fun insertRooms(rooms: List<Room>)
	@Insert fun insertSubjects(subjects: List<Subject>)
	@Insert fun insertTeachers(teachers: List<Teacher>)
	@Insert fun insertTeachingMethods(teachingMethods: List<TeachingMethod>)
	@Insert fun insertSchoolYears(schoolYears: List<SchoolYear>)

	@Transaction
	fun insertUserData(userId: Long, masterData: UntisMasterData) {
		insertAbsenceReasons((masterData.absenceReasons ?: emptyList()).map { it.copy(userId = userId) })
		insertDepartments((masterData.departments ?: emptyList()).map { it.copy(userId = userId) })
		insertDuties((masterData.duties ?: emptyList()).map { it.copy(userId = userId) })
		insertEventReasons((masterData.eventReasons ?: emptyList()).map { it.copy(userId = userId) })
		insertEventReasonGroups((masterData.eventReasonGroups ?: emptyList()).map { it.copy(userId = userId) })
		insertExcuseStatuses((masterData.excuseStatuses ?: emptyList()).map { it.copy(userId = userId) })
		insertHolidays((masterData.holidays ?: emptyList()).map { it.copy(userId = userId) })
		insertKlassen((masterData.klassen).map { it.copy(userId = userId) })
		insertRooms((masterData.rooms).map { it.copy(userId = userId) })
		insertSubjects((masterData.subjects).map { it.copy(userId = userId) })
		insertTeachers((masterData.teachers).map { it.copy(userId = userId) })
		insertTeachingMethods((masterData.teachingMethods ?: emptyList()).map { it.copy(userId = userId) })
		insertSchoolYears((masterData.schoolyears ?: emptyList()).map { it.copy(userId = userId) })
	}

	@Update
	fun update(user: User)

	@Delete
	fun delete(user: User)

	@Delete fun deleteAbsenceReasons(absenceReasons: List<AbsenceReason>)
	@Delete fun deleteDepartments(departments: List<Department>)
	@Delete fun deleteDuties(duties: List<Duty>)
	@Delete fun deleteEventReasons(eventReasons: List<EventReason>)
	@Delete fun deleteEventReasonGroups(eventReasonGroups: List<EventReasonGroup>)
	@Delete fun deleteExcuseStatuses(excuseStatuses: List<ExcuseStatus>)
	@Delete fun deleteHolidays(holidays: List<Holiday>)
	@Delete fun deleteKlassen(klassen: List<Klasse>)
	@Delete fun deleteRooms(rooms: List<Room>)
	@Delete fun deleteSubjects(subjects: List<Subject>)
	@Delete fun deleteTeachers(teachers: List<Teacher>)
	@Delete fun deleteTeachingMethods(teachingMethods: List<TeachingMethod>)
	@Delete fun deleteSchoolYears(schoolYears: List<SchoolYear>)

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
