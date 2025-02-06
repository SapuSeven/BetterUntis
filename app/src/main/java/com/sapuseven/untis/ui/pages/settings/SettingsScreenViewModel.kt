package com.sapuseven.untis.ui.pages.settings

import android.annotation.SuppressLint
import androidx.compose.material3.ColorScheme
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.components.ElementPicker
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.data.model.github.GitHubApi.URL_GITHUB_REPOSITORY_API
import com.sapuseven.untis.data.model.github.GitHubUser
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.services.AutoMuteService
import com.sapuseven.untis.services.AutoMuteServiceZenRuleImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Named

@SuppressLint("NewApi")
@HiltViewModel(assistedFactory = SettingsScreenViewModel.Factory::class)
class SettingsScreenViewModel @AssistedInject constructor(
	userSettingsRepositoryFactory: UserSettingsRepository.Factory,
	globalSettingsRepository: GlobalSettingsRepository,
	private val userScopeManager: UserScopeManager,
	private val userDao: UserDao,
	val autoMuteService: AutoMuteService,
	@Named("json") private val httpClient: HttpClient,
	val savedStateHandle: SavedStateHandle,
	@Assisted val colorScheme: ColorScheme,
) : ViewModel() {
	@AssistedFactory
	interface Factory {
		fun create(colorScheme: ColorScheme): SettingsScreenViewModel
	}

	init {
		if (autoMuteService is AutoMuteServiceZenRuleImpl) {
			autoMuteService.setUser(userScopeManager.user)
		}
	}

	val repository = userSettingsRepositoryFactory.create(colorScheme)
	val globalRepository = globalSettingsRepository

	private val _contributors = MutableStateFlow<List<GitHubUser>>(emptyList())
	val contributors: StateFlow<List<GitHubUser>> = _contributors

	private val _contributorsError = MutableStateFlow<Throwable?>(null)
	val contributorsError: StateFlow<Throwable?> = _contributorsError

	val elementPicker: ElementPicker
		get() = ElementPicker(userScopeManager.user, userDao)

	fun resetColors() = viewModelScope.launch {
		repository.getSettingsDefaults().let { defaults ->
			repository.updateSettings {
				backgroundRegular = defaults.backgroundRegular
				backgroundRegularPast = defaults.backgroundRegularPast
				backgroundExam = defaults.backgroundExam
				backgroundExamPast = defaults.backgroundExamPast
				backgroundIrregular = defaults.backgroundIrregular
				backgroundIrregularPast = defaults.backgroundIrregularPast
				backgroundCancelled = defaults.backgroundCancelled
				backgroundCancelledPast = defaults.backgroundCancelledPast
			}
		}
	}

	suspend fun loadContributors() {
		_contributorsError.value = null

		try {
			_contributors.value = httpClient.get("$URL_GITHUB_REPOSITORY_API/contributors").body()
		} catch (e: Exception) {
			_contributorsError.value = e
		}
	}
}
