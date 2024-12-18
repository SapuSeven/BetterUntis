package com.sapuseven.untis.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.annotations.UserScope
import com.sapuseven.untis.components.UserComponent
import com.sapuseven.untis.mappers.TimetableMapper
import com.sapuseven.untis.ui.activities.settings.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

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
