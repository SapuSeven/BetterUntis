package com.sapuseven.untis.data.repository

import com.sapuseven.untis.api.client.TimetableApi
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.scope.UserScopeManager
import java.io.File
import javax.inject.Inject
import javax.inject.Named

interface InfoCenterRepository {
}

class UntisInfoCenterRepository @Inject constructor(
	private val api: TimetableApi,
	@Named("cacheDir") private val cacheDir: File,
	userScopeManager: UserScopeManager
) : InfoCenterRepository {
	private val user: User = userScopeManager.user
}
