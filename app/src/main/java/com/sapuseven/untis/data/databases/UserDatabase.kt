package com.sapuseven.untis.data.databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.sapuseven.untis.helpers.UserDatabaseQueryHelper.generateCreateTable
import com.sapuseven.untis.helpers.UserDatabaseQueryHelper.generateDropTable
import com.sapuseven.untis.helpers.UserDatabaseQueryHelper.generateValues
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import com.sapuseven.untis.interfaces.TableModel
import com.sapuseven.untis.models.untis.MasterData
import com.sapuseven.untis.models.untis.Settings
import com.sapuseven.untis.models.untis.UserData
import com.sapuseven.untis.models.untis.masterdata.*

private const val DATABASE_VERSION = 1
private const val DATABASE_NAME = "userdata.db"

class UserDatabase private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
	companion object {
		const val COLUMN_NAME_USER_ID = "_user_id"

		private var instance: UserDatabase? = null

		fun createInstance(context: Context): UserDatabase {
			return instance ?: UserDatabase(context)
		}
	}

	override fun onCreate(db: SQLiteDatabase) {
		db.execSQL(UserDatabaseContract.Users.SQL_CREATE_ENTRIES)

		db.execSQL(generateCreateTable<AbsenceReason>())
		db.execSQL(generateCreateTable<Department>())
		db.execSQL(generateCreateTable<Duty>())
		db.execSQL(generateCreateTable<EventReason>())
		db.execSQL(generateCreateTable<EventReasonGroup>())
		db.execSQL(generateCreateTable<ExcuseStatus>())
		db.execSQL(generateCreateTable<Holiday>())
		db.execSQL(generateCreateTable<Klasse>())
		db.execSQL(generateCreateTable<Room>())
		db.execSQL(generateCreateTable<Subject>())
		db.execSQL(generateCreateTable<Teacher>())
		db.execSQL(generateCreateTable<TeachingMethod>())
		db.execSQL(generateCreateTable<SchoolYear>())
	}

	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		// If you change the DATABASE_VERSION, insert logic to migrate the data
		db.execSQL(UserDatabaseContract.Users.SQL_DELETE_ENTRIES)

		db.execSQL(generateDropTable<AbsenceReason>())
		db.execSQL(generateDropTable<Department>())
		db.execSQL(generateDropTable<Duty>())
		db.execSQL(generateDropTable<EventReason>())
		db.execSQL(generateDropTable<EventReasonGroup>())
		db.execSQL(generateDropTable<ExcuseStatus>())
		db.execSQL(generateDropTable<Holiday>())
		db.execSQL(generateDropTable<Klasse>())
		db.execSQL(generateDropTable<Room>())
		db.execSQL(generateDropTable<Subject>())
		db.execSQL(generateDropTable<Teacher>())
		db.execSQL(generateDropTable<TeachingMethod>())
		db.execSQL(generateDropTable<SchoolYear>())

		onCreate(db)
	}

	fun addUser(user: User): Long? {
		//if (!true) // TODO: Remove after testing
		//return null

		val db = writableDatabase

		val values = ContentValues()
		values.put(UserDatabaseContract.Users.COLUMN_NAME_URL, user.url)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_APIURL, user.url)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_SCHOOL, user.school)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_USER, user.user)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_KEY, user.key)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_ANONYMOUS, user.anonymous)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_TIMEGRID, getJSON().stringify(TimeGrid.serializer(), user.timeGrid))
		values.put(UserDatabaseContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP, user.masterDataTimestamp)
		values.put(UserDatabaseContract.Users.COLUMN_NAME_USERDATA, getJSON().stringify(UserData.serializer(), user.userData))
		values.put(UserDatabaseContract.Users.COLUMN_NAME_SETTINGS, getJSON().stringify(Settings.serializer(), user.settings))

		val id = db.insert(UserDatabaseContract.Users.TABLE_NAME, null, values)

		db.close()

		return if (id == -1L)
			null
		else
			id
	}

	fun getUser(id: Long): User? {
		val db = this.readableDatabase

		val cursor = db.query(
				UserDatabaseContract.Users.TABLE_NAME,
				arrayOf(
						UserDatabaseContract.Users.COLUMN_NAME_URL,
						UserDatabaseContract.Users.COLUMN_NAME_APIURL,
						UserDatabaseContract.Users.COLUMN_NAME_SCHOOL,
						UserDatabaseContract.Users.COLUMN_NAME_USER,
						UserDatabaseContract.Users.COLUMN_NAME_KEY,
						UserDatabaseContract.Users.COLUMN_NAME_ANONYMOUS,
						UserDatabaseContract.Users.COLUMN_NAME_TIMEGRID,
						UserDatabaseContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP,
						UserDatabaseContract.Users.COLUMN_NAME_USERDATA,
						UserDatabaseContract.Users.COLUMN_NAME_SETTINGS,
						UserDatabaseContract.Users.COLUMN_NAME_CREATED
				),
				BaseColumns._ID + "=?",
				arrayOf(id.toString()), null, null, UserDatabaseContract.Users.COLUMN_NAME_CREATED + " DESC")

		if (!cursor.moveToFirst())
			return null

		val user = User(
				id,
				cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_URL)),
				cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_APIURL)),
				cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_SCHOOL)),
				cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_USER)),
				cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_KEY)),
				cursor.getInt(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_ANONYMOUS)) == 1,
				getJSON().parse(TimeGrid.serializer(), cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_TIMEGRID))),
				cursor.getLong(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP)),
				getJSON().parse(UserData.serializer(), cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_USERDATA))),
				getJSON().parse(Settings.serializer(), cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_SETTINGS))),
				cursor.getLong(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_CREATED))
		)

		cursor.close()
		db.close()

		return user
	}

	fun getAllUsers(): List<User> {
		val users = ArrayList<User>()
		val db = this.readableDatabase

		val cursor = db.query(
				UserDatabaseContract.Users.TABLE_NAME,
				arrayOf(
						BaseColumns._ID,
						UserDatabaseContract.Users.COLUMN_NAME_URL,
						UserDatabaseContract.Users.COLUMN_NAME_APIURL,
						UserDatabaseContract.Users.COLUMN_NAME_SCHOOL,
						UserDatabaseContract.Users.COLUMN_NAME_USER,
						UserDatabaseContract.Users.COLUMN_NAME_KEY,
						UserDatabaseContract.Users.COLUMN_NAME_ANONYMOUS,
						UserDatabaseContract.Users.COLUMN_NAME_TIMEGRID,
						UserDatabaseContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP,
						UserDatabaseContract.Users.COLUMN_NAME_USERDATA,
						UserDatabaseContract.Users.COLUMN_NAME_SETTINGS,
						UserDatabaseContract.Users.COLUMN_NAME_CREATED
				), null, null, null, null, UserDatabaseContract.Users.COLUMN_NAME_CREATED + " DESC")

		if (cursor.moveToFirst()) {
			do {
				users.add(User(
						cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)),
						cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_URL)),
						cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_APIURL)),
						cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_SCHOOL)),
						cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_USER)),
						cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_KEY)),
						cursor.getInt(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_ANONYMOUS)) == 1,
						getJSON().parse(TimeGrid.serializer(), cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_TIMEGRID))),
						cursor.getLong(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP)),
						getJSON().parse(UserData.serializer(), cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_USERDATA))),
						getJSON().parse(Settings.serializer(), cursor.getString(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_SETTINGS))),
						cursor.getLong(cursor.getColumnIndex(UserDatabaseContract.Users.COLUMN_NAME_CREATED))
				))
			} while (cursor.moveToNext())
		}

		cursor.close()
		db.close()

		return users
	}

	fun getUsersCount(): Int {
		val db = this.readableDatabase

		val cursor = db.query(
				UserDatabaseContract.Users.TABLE_NAME,
				arrayOf(BaseColumns._ID), null, null, null, null, null)

		val count = cursor.count
		cursor.close()
		db.close()

		return count
	}

	fun setAdditionalUserData(
			userId: Long,
			masterData: MasterData
	) {
		val db = writableDatabase
		db.beginTransaction()

		var tableName = AbsenceReason.TABLE_NAME
		tableName.let { masterData.absenceReasons.forEach { data -> db.insert(tableName, null, generateValues(userId, data)) } }
		tableName = Department.TABLE_NAME
		tableName.let { masterData.departments.forEach { data -> db.insert(tableName, null, generateValues(userId, data)) } }
		tableName = Duty.TABLE_NAME
		tableName.let { masterData.duties.forEach { data -> db.insert(tableName, null, generateValues(userId, data)) } }
		tableName = EventReason.TABLE_NAME
		tableName.let { masterData.eventReasons.forEach { data -> db.insert(tableName, null, generateValues(userId, data)) } }
		tableName = EventReasonGroup.TABLE_NAME
		tableName.let { masterData.eventReasonGroups.forEach { data -> db.insert(tableName, null, generateValues(userId, data)) } }
		tableName = ExcuseStatus.TABLE_NAME
		tableName.let { masterData.excuseStatuses.forEach { data -> db.insert(tableName, null, generateValues(userId, data)) } }
		tableName = Holiday.TABLE_NAME
		tableName.let { masterData.holidays.forEach { data -> db.insert(tableName, null, generateValues(userId, data)) } }
		tableName = Klasse.TABLE_NAME
		tableName.let { masterData.klassen.forEach { data -> db.insert(tableName, null, generateValues(userId, data)) } }
		tableName = Room.TABLE_NAME
		tableName.let { masterData.rooms.forEach { data -> db.insert(tableName, null, generateValues(userId, data)) } }
		tableName = Subject.TABLE_NAME
		tableName.let { masterData.subjects.forEach { data -> db.insert(tableName, null, generateValues(userId, data)) } }
		tableName = Teacher.TABLE_NAME
		tableName.let { masterData.teachers.forEach { data -> db.insert(tableName, null, generateValues(userId, data)) } }
		tableName = TeachingMethod.TABLE_NAME
		tableName.let { masterData.teachingMethods.forEach { data -> db.insert(tableName, null, generateValues(userId, data)) } }
		tableName = SchoolYear.TABLE_NAME
		tableName.let { masterData.schoolyears.forEach { data -> db.insert(tableName, null, generateValues(userId, data)) } }

		val values = ContentValues()
		values.put(UserDatabaseContract.Users.COLUMN_NAME_MASTERDATATIMESTAMP, masterData.timeStamp)
		db.update(
				UserDatabaseContract.Users.TABLE_NAME,
				values,
				BaseColumns._ID + "=?",
				arrayOf(userId.toString()))

		db.setTransactionSuccessful()
		db.endTransaction()
		db.close()
	}


	inline fun <reified T : TableModel> getAdditionalUserData(userId: Long, table: TableModel): Map<Int, T>? {
		val db = readableDatabase

		val cursor = db.query(
				table.getTableName(),
				table.generateValues().keySet().toTypedArray(), "$COLUMN_NAME_USER_ID=?",
				arrayOf(userId.toString()), null, null, "id DESC")

		if (!cursor.moveToFirst())
			return null

		val result = mutableMapOf<Int, T>()

		if (cursor.moveToFirst()) {
			do {
				val data = table.parseCursor(cursor) as T
				result[(data as TableModel).getElementId()] = data
			} while (cursor.moveToNext())
		}

		cursor.close()
		db.close()

		return result.toMap()
	}
}

// TODO: Move to UserDatabase class
class User(
		val id: Long? = null,
		val url: String? = null,
		val apiUrl: String? = null,
		val school: String,
		val user: String? = null,
		val key: String? = null,
		val anonymous: Boolean = false,
		val timeGrid: TimeGrid,
		val masterDataTimestamp: Long,
		val userData: UserData,
		val settings: Settings,
		val created: Long? = null
)

