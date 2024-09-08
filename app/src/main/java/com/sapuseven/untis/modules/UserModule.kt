package com.sapuseven.untis.modules

import com.sapuseven.untis.components.ElementPicker
import com.sapuseven.untis.components.UserComponent
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.databases.entities.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn

@Module
@InstallIn(UserComponent::class)
class UserModule {
	@Provides
	fun provideElementPicker(user: User, userDao: UserDao): ElementPicker {
		return ElementPicker(user, userDao)
	}
}

@InstallIn(UserComponent::class)
@EntryPoint
interface UserComponentEntryPoint {
	fun getUser(): User
}
