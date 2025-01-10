package com.sapuseven.untis.modules

import com.sapuseven.untis.api.client.SchoolSearchApi
import com.sapuseven.untis.api.client.TimetableApi
import com.sapuseven.untis.api.client.UserDataApi
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.data.repository.ElementRepository
import com.sapuseven.untis.data.repository.ElementRepositoryImpl
import com.sapuseven.untis.scope.UserScopeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import io.ktor.client.engine.cio.CIO

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
	@Provides
	fun provideElementRepository(
		userDao: UserDao,
		userScopeManager: UserScopeManager
	): ElementRepository = ElementRepositoryImpl(userDao, userScopeManager)
}
