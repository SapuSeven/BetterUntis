package com.sapuseven.untis.modules

import com.sapuseven.untis.components.ElementPicker
import com.sapuseven.untis.components.UserComponent
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.databases.entities.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn

@EntryPoint
@InstallIn(UserComponent::class)
interface UserComponentEntryPoint {
	fun getUser(): User
}
