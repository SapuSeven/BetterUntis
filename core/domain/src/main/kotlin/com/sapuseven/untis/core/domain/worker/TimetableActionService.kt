package com.sapuseven.untis.core.domain.worker

import com.sapuseven.untis.core.model.user.User

interface TimetableActionService {
	suspend fun triggerActions(user: User)
}
