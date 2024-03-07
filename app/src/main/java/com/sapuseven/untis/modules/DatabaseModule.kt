package com.sapuseven.untis.modules

import android.content.Context
import androidx.room.Room
import com.sapuseven.untis.data.databases.MIGRATIONS_LEGACY
import com.sapuseven.untis.data.databases.MIGRATION_7_8
import com.sapuseven.untis.data.databases.UserDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
	@Provides
	fun provideUserDatabase(
		@ApplicationContext context: Context
	) = Room.databaseBuilder(
		context,
		UserDatabase::class.java, "userdata.db"
	)
		//.allowMainThreadQueries()
		.addMigrations(
			*MIGRATIONS_LEGACY.toTypedArray(),
			MIGRATION_7_8,
		)
		.build()

	@Provides
	fun provideUserDao(db: UserDatabase) = db.userDao()
}
