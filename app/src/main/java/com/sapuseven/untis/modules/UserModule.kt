package com.sapuseven.untis.modules

import com.sapuseven.untis.components.UserComponent
import com.sapuseven.untis.data.database.entities.User
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn

@EntryPoint
@InstallIn(UserComponent::class)
interface UserComponentEntryPoint {
	fun getUser(): User
}
