package com.sapuseven.untis.data.databases.entities

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.room.*
import com.sapuseven.untis.R
import com.sapuseven.untis.models.TimetableBookmark
import com.sapuseven.untis.models.untis.UntisSettings
import com.sapuseven.untis.models.untis.UntisUserData
import com.sapuseven.untis.models.untis.masterdata.TimeGrid

@Entity
data class User (
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

@Dao
interface UserDao {
	@Query("SELECT * FROM user")
	fun getAll(): List<User>

	@Query("SELECT * FROM user WHERE id LIKE :userId")
	fun getById(userId: Long): List<User>

	@Insert
	fun insert(user: User)

	@Update
	fun update(user: User)

	@Delete
	fun delete(user: User)
}
