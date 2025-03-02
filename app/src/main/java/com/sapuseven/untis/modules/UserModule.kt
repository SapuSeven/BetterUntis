package com.sapuseven.untis.modules

import com.sapuseven.untis.components.UserComponent
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.scope.UserScopeManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface UserModule {
	@Singleton
	@Binds
	fun bindUserScopeManager(
		implementation: UserScopeManagerImpl
	): UserScopeManager
}

@EntryPoint
@InstallIn(UserComponent::class)
interface UserComponentEntryPoint {
	fun getUser(): User
}
