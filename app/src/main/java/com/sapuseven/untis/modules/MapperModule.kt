package com.sapuseven.untis.modules

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object UserScopedMapperModule {
	/*@Provides
	@ViewModelScoped // This ties the scope to the ViewModel lifecycle
	fun provideCoroutineScope(): CoroutineScope {
		return CoroutineScope(Dispatchers.IO + SupervisorJob())
	}

	@Provides
	fun provideTimetableMapper(
		repository: SettingsRepository,
		@ViewModelScoped scope: CoroutineScope
	): TimetableMapper = TimetableMapper(
		repository = repository,
		scope = scope
	)*/
}
