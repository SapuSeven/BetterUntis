package com.sapuseven.untis.persistence.entity

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
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
import androidx.room.Upsert
import com.sapuseven.untis.api.model.untis.MasterData
import com.sapuseven.untis.api.model.untis.SchoolInfo
import com.sapuseven.untis.api.model.untis.Settings
import com.sapuseven.untis.api.model.untis.UserData
import com.sapuseven.untis.api.model.untis.masterdata.TimeGrid
import kotlinx.coroutines.flow.Flow

@Entity
data class User(
	@PrimaryKey(autoGenerate = true) val id: Long,
	val profileName: String = "",
	val apiHost: String, // When populated before schema version 12, this may be a full URL, not just the host
	val schoolInfo: SchoolInfo? = null,
	@Deprecated(
		"Not populated with schema version 12",
		ReplaceWith("schoolInfo.schoolId")
	) val schoolId: String? = null,
	val user: String? = null,
	val key: String? = null,
	val anonymous: Boolean = false,
	val timeGrid: TimeGrid,
	val masterDataTimestamp: Long,
	val userData: UserData,
	val settings: Settings? = null,
	val created: Long? = null,
) {
	companion object {
		fun buildApiUrl(
			apiHost: String,
			schoolInfo: SchoolInfo? = null
		): Uri {

			val host = apiHost.ifBlank {
				if (schoolInfo?.useMobileServiceUrlAndroid == true && !schoolInfo.mobileServiceUrl.isNullOrBlank()) schoolInfo.mobileServiceUrl
				else schoolInfo?.serverUrl
			}!!.toUri().host

			return Uri.Builder()
				.scheme("https")
				.authority(host)
				.appendPath("WebUntis")
				.build()
		}

		fun buildJsonRpcApiUrl(apiUrl: Uri, schoolName: String): Uri {
			return apiUrl.buildUpon()
				.appendEncodedPath("jsonrpc_intern.do")
				.appendQueryParameter("school", schoolName)
				.build()
		}
	}

	@Deprecated("TODO")
	fun getDisplayedName(context: Context): String {
		return when {
			profileName.isNotBlank() -> profileName
			anonymous -> "(anonymous)"// TODO find a solution without resource strings for context.getString(R.string.all_anonymous)
			else -> userData.displayName
		}
	}

	@Deprecated("Use getDisplayedName() with context")
	fun getDisplayedName(): String {
		return when {
			profileName.isNotBlank() -> profileName
			anonymous -> "(anonymous)"// TODO find a solution without resource strings for stringResource(R.string.all_anonymous)
			else -> userData.displayName
		}
	}

	val apiUrl: Uri by lazy {
		buildApiUrl(apiHost, schoolInfo)
	}

	val jsonRpcApiUrl: Uri by lazy {
		val schoolName = schoolInfo?.loginName ?: apiHost.toUri().getQueryParameter("school") ?: ""
		buildJsonRpcApiUrl(apiUrl, schoolName)
	}

	val restApiUrl: Uri by lazy {
		apiUrl.buildUpon()
			.appendEncodedPath("api/rest")
			.build()
	}

	val restApiAuthUrl: Uri by lazy {
		apiUrl.buildUpon()
			.appendEncodedPath("api/mobile/v2")
			.build()
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

	@Query("SELECT * FROM user")
	suspend fun getAllAsync(): List<User>

	@Deprecated("Should be migrated to getAllAsync() or getAllFlow()")
	@Query("SELECT * FROM user")
	fun getAll(): List<User>

	// TODO: Make suspend
	@Query("SELECT * FROM user WHERE id LIKE :userId")
	fun getById(userId: Long): User?

	@Query("SELECT * FROM user WHERE id LIKE :userId")
	suspend fun getByIdAsync(userId: Long): User?

	@Transaction
	@Query("SELECT * FROM user")
	suspend fun getAllWithData(): List<UserWithData>

	@Transaction
	@Query("SELECT * FROM user WHERE id LIKE :userId")
	suspend fun getByIdWithData(userId: Long): UserWithData?

	@Transaction
	@Query("SELECT * FROM user WHERE id LIKE :userId")
	fun getByIdWithDataFlow(userId: Long): Flow<UserWithData?>

	@Insert
	suspend fun insert(user: User): Long

	@Insert
	suspend fun insertAbsenceReasons(absenceReasons: List<AbsenceReasonEntity>)

	@Insert
	suspend fun insertDepartments(departments: List<DepartmentEntity>)

	@Insert
	suspend fun insertDuties(duties: List<DutyEntity>)

	@Insert
	suspend fun insertEventReasons(eventReasons: List<EventReasonEntity>)

	@Insert
	suspend fun insertEventReasonGroups(eventReasonGroups: List<EventReasonGroupEntity>)

	@Insert
	suspend fun insertExcuseStatuses(excuseStatuses: List<ExcuseStatusEntity>)

	@Insert
	suspend fun insertHolidays(holidays: List<HolidayEntity>)

	@Insert
	suspend fun insertKlassen(klassen: List<KlasseEntity>)

	@Insert
	suspend fun insertRooms(rooms: List<RoomEntity>)

	@Insert
	suspend fun insertSubjects(subjects: List<SubjectEntity>)

	@Insert
	suspend fun insertTeachers(teachers: List<TeacherEntity>)

	@Insert
	suspend fun insertTeachingMethods(teachingMethods: List<TeachingMethodEntity>)

	@Insert
	suspend fun insertSchoolYears(schoolYears: List<SchoolYearEntity>)

	@Upsert
	suspend fun upsertAbsenceReasons(absenceReasons: List<AbsenceReasonEntity>)

	@Upsert
	suspend fun upsertDepartments(departments: List<DepartmentEntity>)

	@Upsert
	suspend fun upsertDuties(duties: List<DutyEntity>)

	@Upsert
	suspend fun upsertEventReasons(eventReasons: List<EventReasonEntity>)

	@Upsert
	suspend fun upsertEventReasonGroups(eventReasonGroups: List<EventReasonGroupEntity>)

	@Upsert
	suspend fun upsertExcuseStatuses(excuseStatuses: List<ExcuseStatusEntity>)

	@Upsert
	suspend fun upsertHolidays(holidays: List<HolidayEntity>)

	@Upsert
	suspend fun upsertKlassen(klassen: List<KlasseEntity>)

	@Upsert
	suspend fun upsertRooms(rooms: List<RoomEntity>)

	@Upsert
	suspend fun upsertSubjects(subjects: List<SubjectEntity>)

	@Upsert
	suspend fun upsertTeachers(teachers: List<TeacherEntity>)

	@Upsert
	suspend fun upsertTeachingMethods(teachingMethods: List<TeachingMethodEntity>)

	@Upsert
	suspend fun upsertSchoolYears(schoolYears: List<SchoolYearEntity>)

	@Transaction
	suspend fun insertMasterData(userId: Long, masterData: MasterData) {
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
		updateMasterDataTimestamp(userId, masterData.timeStamp)
	}

	@Transaction
	suspend fun upsertMasterData(userId: Long, masterData: MasterData) {
		upsertAbsenceReasons(masterData.absenceReasons.orEmpty().map { AbsenceReasonEntity.map(it, userId) })
		upsertDepartments(masterData.departments.orEmpty().map { DepartmentEntity.map(it, userId) })
		upsertDuties(masterData.duties.orEmpty().map { DutyEntity.map(it, userId) })
		upsertEventReasons(masterData.eventReasons.orEmpty().map { EventReasonEntity.map(it, userId) })
		upsertEventReasonGroups(masterData.eventReasonGroups.orEmpty().map { EventReasonGroupEntity.map(it, userId) })
		upsertExcuseStatuses(masterData.excuseStatuses.orEmpty().map { ExcuseStatusEntity.map(it, userId) })
		upsertHolidays(masterData.holidays.orEmpty().map { HolidayEntity.map(it, userId) })
		upsertKlassen(masterData.klassen.map { KlasseEntity.map(it, userId) })
		upsertRooms(masterData.rooms.map { RoomEntity.map(it, userId) })
		upsertSubjects(masterData.subjects.map { SubjectEntity.map(it, userId) })
		upsertTeachers(masterData.teachers.map { TeacherEntity.map(it, userId) })
		upsertTeachingMethods(masterData.teachingMethods.orEmpty().map { TeachingMethodEntity.map(it, userId) })
		upsertSchoolYears(masterData.schoolyears.orEmpty().map { SchoolYearEntity.map(it, userId) })
		updateMasterDataTimestamp(userId, masterData.timeStamp)
	}

	@Query("UPDATE user SET masterDataTimestamp = :timestamp WHERE id = :userId")
	suspend fun updateMasterDataTimestamp(userId: Long, timestamp: Long)

	@Update
	suspend fun update(user: User)

	@Delete
	suspend fun delete(user: User)

	@Delete
	suspend fun deleteAbsenceReasons(absenceReasons: List<AbsenceReasonEntity>)

	@Delete
	suspend fun deleteDepartments(departments: List<DepartmentEntity>)

	@Delete
	suspend fun deleteDuties(duties: List<DutyEntity>)

	@Delete
	suspend fun deleteEventReasons(eventReasons: List<EventReasonEntity>)

	@Delete
	suspend fun deleteEventReasonGroups(eventReasonGroups: List<EventReasonGroupEntity>)

	@Delete
	suspend fun deleteExcuseStatuses(excuseStatuses: List<ExcuseStatusEntity>)

	@Delete
	suspend fun deleteHolidays(holidays: List<HolidayEntity>)

	@Delete
	suspend fun deleteKlassen(klassen: List<KlasseEntity>)

	@Delete
	suspend fun deleteRooms(rooms: List<RoomEntity>)

	@Delete
	suspend fun deleteSubjects(subjects: List<SubjectEntity>)

	@Delete
	suspend fun deleteTeachers(teachers: List<TeacherEntity>)

	@Delete
	suspend fun deleteTeachingMethods(teachingMethods: List<TeachingMethodEntity>)

	@Delete
	suspend fun deleteSchoolYears(schoolYears: List<SchoolYearEntity>)

	@Transaction
	suspend fun deleteUserData(userWithData: UserWithData) {
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
	suspend fun deleteUserData(userId: Long) = getByIdWithData(userId)?.let { deleteUserData(it) }
}
