package com.sapuseven.untis.modules

import com.sapuseven.untis.annotations.UserScope
import com.sapuseven.untis.components.UserComponent
import com.sapuseven.untis.mappers.TimetableMapper
import com.sapuseven.untis.ui.activities.settings.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(UserComponent::class)
object UserScopedMapperModule {
	@Provides
	fun provideTimetableMapper(
		repository: SettingsRepository
	): TimetableMapper = TimetableMapper(repository)
}
