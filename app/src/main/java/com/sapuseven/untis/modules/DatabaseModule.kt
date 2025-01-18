package com.sapuseven.untis.modules

import android.content.Context
import androidx.room.Room
import com.sapuseven.untis.data.database.MIGRATIONS_USER_LEGACY
import com.sapuseven.untis.data.database.MIGRATION_USER_7_8
import com.sapuseven.untis.data.database.RoomFinderDatabase
import com.sapuseven.untis.data.database.UserDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserDatabaseModule {
	@Provides
	@Singleton
	fun provideUserDatabase(
		@ApplicationContext context: Context
	) = Room.databaseBuilder(
		context,
		UserDatabase::class.java, "userdata.db"
	)
		.addMigrations(
			*MIGRATIONS_USER_LEGACY.toTypedArray(),
			MIGRATION_USER_7_8,
		)
		.build()

	@Provides
	@Singleton
	fun provideUserDao(db: UserDatabase) = db.userDao()
}

@Module
@InstallIn(SingletonComponent::class)
object RoomFinderDatabaseModule {
	@Provides
	@Singleton
	fun provideRoomFinderDatabase(
		@ApplicationContext context: Context
	) = Room.databaseBuilder(
		context,
		RoomFinderDatabase::class.java, "roomfinder.db"
	)
		// In old versions, each profile has their own roomfinder database file.
		// This makes it hard to run an automatic migration.
		// Since the data can be recovered easily, migration is skipped and a new database is used.
		.build()

	@Provides
	@Singleton
	fun provideRoomFinderDao(db: RoomFinderDatabase) = db.roomFinderDao()
}
