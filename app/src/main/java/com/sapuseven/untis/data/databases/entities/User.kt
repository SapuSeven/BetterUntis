package com.sapuseven.untis.data.databases.entities

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.room.*
import androidx.room.Room
import com.sapuseven.untis.R
import com.sapuseven.untis.models.TimetableBookmark
import com.sapuseven.untis.models.untis.UntisSettings
import com.sapuseven.untis.models.untis.UntisUserData
import com.sapuseven.untis.models.untis.masterdata.*

@Entity
data class User(
	@PrimaryKey val id: Long = -1,
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

	@Relation(parentColumn = "id", entityColumn = "id")
	val absenceReasons: List<AbsenceReason>,

	@Relation(parentColumn = "id", entityColumn = "id")
	val departments: List<Department>,

	@Relation(parentColumn = "id", entityColumn = "id")
	val duties: List<Duty>,

	@Relation(parentColumn = "id", entityColumn = "id")
	val eventReasons: List<EventReason>,

	@Relation(parentColumn = "id", entityColumn = "id")
	val eventReasonGroups: List<EventReasonGroup>,

	@Relation(parentColumn = "id", entityColumn = "id")
	val excuseStatuss: List<ExcuseStatus>,

	@Relation(parentColumn = "id", entityColumn = "id")
	val holidays: List<Holiday>,

	@Relation(parentColumn = "id", entityColumn = "id")
	val klasses: List<Klasse>,

	@Relation(parentColumn = "id", entityColumn = "id")
	val rooms: List<  com.sapuseven.untis.models.untis.masterdata.Room>,

	@Relation(parentColumn = "id", entityColumn = "id")
	val subjects: List<Subject>,

	@Relation(parentColumn = "id", entityColumn = "id")
	val teachers: List<Teacher>,

	@Relation(parentColumn = "id", entityColumn = "id")
	val teachingMethods: List<TeachingMethod>,

	@Relation(parentColumn = "id", entityColumn = "id")
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
	fun insert(user: User)

	@Update
	fun update(user: User)

	@Delete
	fun delete(user: User)
}
