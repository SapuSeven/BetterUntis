package com.sapuseven.untis.models.untis.masterdata

import android.content.ContentValues
import android.database.Cursor
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sapuseven.untis.annotations.Table
import com.sapuseven.untis.annotations.TableColumn
import com.sapuseven.untis.data.databases.TABLE_NAME_ABSENCE_REASONS
import com.sapuseven.untis.interfaces.TableModel
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class AbsenceReason(
		@PrimaryKey val id: Int,
		val name: String,
		val longName: String,
		val active: Boolean
)
