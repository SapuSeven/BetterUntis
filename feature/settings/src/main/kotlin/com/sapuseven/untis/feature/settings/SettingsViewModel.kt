package com.sapuseven.untis.feature.settings

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.core.datastore.GlobalSettingsDataSource
import com.sapuseven.untis.core.datastore.UserSettingsDataSource
import com.sapuseven.untis.core.domain.repository.ElementRepository
import com.sapuseven.untis.core.domain.repository.UserRepository
import com.sapuseven.untis.core.domain.service.AutoMuteService
import com.sapuseven.untis.core.domain.worker.TimetableActionService
import com.sapuseven.untis.core.model.timetable.Element
import com.sapuseven.untis.core.model.timetable.ElementType
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@SuppressLint("NewApi")
@HiltViewModel(assistedFactory = SettingsViewModel.Factory::class)
class SettingsViewModel @AssistedInject constructor(
	@param:ApplicationContext private val context: Context,
	internal val globalSettingsDataSource: GlobalSettingsDataSource,
	internal val userSettingsDataSource: UserSettingsDataSource,
	elementRepository: ElementRepository,
	internal val autoMuteService: AutoMuteService,
	//@Named("json") private val httpClient: HttpClient,
	@Assisted val colorScheme: ColorScheme,
	private val timetableActionService: TimetableActionService,
	private val userRepository: UserRepository,
) : ViewModel() {
	@AssistedFactory
	interface Factory {
		fun create(colorScheme: ColorScheme): SettingsViewModel
	}

	init {
		autoMuteService.setUser(userRepository.getActiveUser())
	}

	val elements: StateFlow<Map<ElementType, List<Element>>> = elementRepository.timetableElements.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = emptyMap()
	)

	val notificationsEnabled: StateFlow<Boolean> = userSettingsDataSource.getSettings()
		.map { it.notificationsEnable }
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000),
			initialValue = false
		)

	suspend fun toggleNotifications(enabled: Boolean) {
		userSettingsDataSource.updateSettings { notificationsEnable = enabled }

		if (enabled) {
			timetableActionService.triggerActions(userRepository.observeActiveUser().firstOrNull() ?: return)
		} else {
			clearNotifications()
		}
	}

	fun clearNotifications() {
		(context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.cancelAll()
	}

	/*private val _contributors = MutableStateFlow<List<GitHubUser>>(emptyList())
	val contributors: StateFlow<List<GitHubUser>> = _contributors

	private val _contributorsError = MutableStateFlow<Throwable?>(null)
	val contributorsError: StateFlow<Throwable?> = _contributorsError*/

	fun resetColors() = viewModelScope.launch {
		userSettingsDataSource.updateSettings {
			clearBackgroundRegular()
			clearBackgroundRegularPast()
			clearBackgroundExam()
			clearBackgroundExamPast()
			clearBackgroundIrregular()
			clearBackgroundIrregularPast()
			clearBackgroundCancelled()
			clearBackgroundCancelledPast()
		}
	}

	suspend fun loadContributors() {
		/* TODO _contributorsError.value = null

		try {
			_contributors.value = httpClient.get("$URL_GITHUB_REPOSITORY_API/contributors").body()
		} catch (e: Exception) {
			_contributorsError.value = e
		}*/
	}
}
