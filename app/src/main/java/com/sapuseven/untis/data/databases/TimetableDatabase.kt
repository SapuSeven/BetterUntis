package com.sapuseven.untis.data.databases


private const val DATABASE_VERSION = 1
private const val DATABASE_NAME = "timetable"

/*class TimetableDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
	companion object {
		const val COLUMN_NAME_USER_ID = "_user_id"
	}

	override fun onCreate(db: SQLiteDatabase) {
		db.execSQL(TimetableDatabaseContract.Users.SQL_CREATE_ENTRIES)

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
		db.execSQL(TimetableDatabaseContract.Users.SQL_DELETE_ENTRIES)

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
		if (!true) // TODO: Remove after testing
			return null

		val db = writableDatabase

		val values = ContentValues()
		values.put(TimetableDatabaseContract.Users.COLUMN_NAME_URL, user.url)
		values.put(TimetableDatabaseContract.Users.COLUMN_NAME_SCHOOL, user.school)
		values.put(TimetableDatabaseContract.Users.COLUMN_NAME_USER, user.user)
		values.put(TimetableDatabaseContract.Users.COLUMN_NAME_KEY, user.key)
		values.put(TimetableDatabaseContract.Users.COLUMN_NAME_ANONYMOUS, user.anonymous)
		values.put(TimetableDatabaseContract.Users.COLUMN_NAME_TIMEGRID, getJSON().stringify(user.timeGrid))
		values.put(TimetableDatabaseContract.Users.COLUMN_NAME_USERDATA, getJSON().stringify(user.userData))
		values.put(TimetableDatabaseContract.Users.COLUMN_NAME_SETTINGS, getJSON().stringify(user.settings))

		val id = db.insert(TimetableDatabaseContract.Users.TABLE_NAME, null, values)

		db.close()

		return if (id == -1L)
			null
		else
			id
	}

	fun getUser(id: Long): User? {
		val db = this.readableDatabase

		val cursor = db.query(
				TimetableDatabaseContract.Users.TABLE_NAME,
				arrayOf(
						TimetableDatabaseContract.Users.COLUMN_NAME_URL,
						TimetableDatabaseContract.Users.COLUMN_NAME_SCHOOL,
						TimetableDatabaseContract.Users.COLUMN_NAME_USER,
						TimetableDatabaseContract.Users.COLUMN_NAME_KEY,
						TimetableDatabaseContract.Users.COLUMN_NAME_ANONYMOUS,
						TimetableDatabaseContract.Users.COLUMN_NAME_TIMEGRID,
						TimetableDatabaseContract.Users.COLUMN_NAME_USERDATA,
						TimetableDatabaseContract.Users.COLUMN_NAME_SETTINGS,
						TimetableDatabaseContract.Users.COLUMN_NAME_CREATED
				),
				BaseColumns._ID + "=?", // TODO: What does =? mean?
				arrayOf(id.toString()), null, null, TimetableDatabaseContract.Users.COLUMN_NAME_CREATED + " DESC")

		if (!cursor.moveToFirst())
			return null

		val user = User(
				cursor.getString(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_URL)),
				cursor.getString(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_SCHOOL)),
				cursor.getString(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_USER)),
				cursor.getString(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_KEY)),
				cursor.getInt(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_ANONYMOUS)) == 1,
				getJSON().parse(cursor.getString(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_TIMEGRID))),
				getJSON().parse(cursor.getString(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_USERDATA))),
				getJSON().parse(cursor.getString(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_SETTINGS))),
				cursor.getLong(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_KEY))
		)

		cursor.close()

		return user
	}

	fun getAllUsers(): List<User> {
		val users = ArrayList<User>()
		val db = this.readableDatabase

		val cursor = db.query(
				TimetableDatabaseContract.Users.TABLE_NAME,
				arrayOf(
						TimetableDatabaseContract.Users.COLUMN_NAME_URL,
						TimetableDatabaseContract.Users.COLUMN_NAME_SCHOOL,
						TimetableDatabaseContract.Users.COLUMN_NAME_USER,
						TimetableDatabaseContract.Users.COLUMN_NAME_KEY,
						TimetableDatabaseContract.Users.COLUMN_NAME_ANONYMOUS,
						TimetableDatabaseContract.Users.COLUMN_NAME_TIMEGRID,
						TimetableDatabaseContract.Users.COLUMN_NAME_USERDATA,
						TimetableDatabaseContract.Users.COLUMN_NAME_SETTINGS,
						TimetableDatabaseContract.Users.COLUMN_NAME_CREATED
				), null, null, null, null, TimetableDatabaseContract.Users.COLUMN_NAME_CREATED + " DESC")

		if (cursor.moveToFirst()) {
			do {
				users.add(User(
						cursor.getString(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_URL)),
						cursor.getString(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_SCHOOL)),
						cursor.getString(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_USER)),
						cursor.getString(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_KEY)),
						cursor.getInt(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_ANONYMOUS)) == 1,
						getJSON().parse(cursor.getString(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_TIMEGRID))),
						getJSON().parse(cursor.getString(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_USERDATA))),
						getJSON().parse(cursor.getString(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_SETTINGS))),
						cursor.getLong(cursor.getColumnIndex(TimetableDatabaseContract.Users.COLUMN_NAME_KEY))
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
				TimetableDatabaseContract.Users.TABLE_NAME,
				arrayOf(BaseColumns._ID), null, null, null, null, null)

		val count = cursor.count
		cursor.close()

		return count
	}

	fun setAdditionalUserData(
			userId: Long,
			masterData: MasterData
	) {
		val db = writableDatabase
		db.beginTransaction()

		var tableName = AbsenceReason.TABLE_NAME
		tableName.let { _ -> masterData.absenceReasons.forEach { db.insert(tableName, null, generateValues(userId, it)) } }
		tableName = Department.TABLE_NAME
		tableName.let { _ -> masterData.departments.forEach { db.insert(tableName, null, generateValues(userId, it)) } }
		tableName = Duty.TABLE_NAME
		tableName.let { _ -> masterData.duties.forEach { db.insert(tableName, null, generateValues(userId, it)) } }
		tableName = EventReason.TABLE_NAME
		tableName.let { _ -> masterData.eventReasons.forEach { db.insert(tableName, null, generateValues(userId, it)) } }
		tableName = EventReasonGroup.TABLE_NAME
		tableName.let { _ -> masterData.eventReasonGroups.forEach { db.insert(tableName, null, generateValues(userId, it)) } }
		tableName = ExcuseStatus.TABLE_NAME
		tableName.let { _ -> masterData.excuseStatuses.forEach { db.insert(tableName, null, generateValues(userId, it)) } }
		tableName = Holiday.TABLE_NAME
		tableName.let { _ -> masterData.holidays.forEach { db.insert(tableName, null, generateValues(userId, it)) } }
		tableName = Klasse.TABLE_NAME
		tableName.let { _ -> masterData.klassen.forEach { db.insert(tableName, null, generateValues(userId, it)) } }
		tableName = Room.TABLE_NAME
		tableName.let { _ -> masterData.rooms.forEach { db.insert(tableName, null, generateValues(userId, it)) } }
		tableName = Subject.TABLE_NAME
		tableName.let { _ -> masterData.subjects.forEach { db.insert(tableName, null, generateValues(userId, it)) } }
		tableName = Teacher.TABLE_NAME
		tableName.let { _ -> masterData.teachers.forEach { db.insert(tableName, null, generateValues(userId, it)) } }
		tableName = TeachingMethod.TABLE_NAME
		tableName.let { _ -> masterData.teachingMethods.forEach { db.insert(tableName, null, generateValues(userId, it)) } }
		tableName = SchoolYear.TABLE_NAME
		tableName.let { _ -> masterData.schoolyears.forEach { db.insert(tableName, null, generateValues(userId, it)) } }

		db.setTransactionSuccessful()
		db.endTransaction()
		db.close()
	}
}

class User(
		val url: String? = null,
		val school: String? = null,
		val user: String? = null,
		val key: String? = null,
		val anonymous: Boolean = false,
		val timeGrid: TimeGrid,
		val userData: UserData,
		val settings: Settings,
		val created: Long? = null
)*/