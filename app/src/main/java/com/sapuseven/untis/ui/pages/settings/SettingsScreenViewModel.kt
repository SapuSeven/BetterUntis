package com.sapuseven.untis.ui.pages.settings

import android.annotation.SuppressLint
import androidx.compose.material3.ColorScheme
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.data.model.github.GitHubApi.URL_GITHUB_REPOSITORY_API
import com.sapuseven.untis.data.model.github.GitHubUser
import com.sapuseven.untis.data.repository.GlobalSettingsRepository
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.data.repository.UserRepository
import com.sapuseven.untis.data.repository.UserSettingsRepository
import com.sapuseven.untis.persistence.entity.ElementEntity
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Named

@SuppressLint("NewApi")
@HiltViewModel(assistedFactory = SettingsScreenViewModel.Factory::class)
class SettingsScreenViewModel @AssistedInject constructor(
	globalSettingsRepository: GlobalSettingsRepository,
	private val userRepository: UserRepository,
	internal val userSettingsRepository: UserSettingsRepository,
	internal val autoMuteService: AutoMuteService,
	internal val savedStateHandle: SavedStateHandle,
	internal val masterDataRepository: MasterDataRepository,
	@Named("json") private val httpClient: HttpClient,
	@Assisted val colorScheme: ColorScheme,
) : ViewModel() {
	@AssistedFactory
	interface Factory {
		fun create(colorScheme: ColorScheme): SettingsScreenViewModel
	}

	init {
		if (autoMuteService is AutoMuteServiceZenRuleImpl) {
			autoMuteService.setUser(userRepository.currentUser!!)
		}
	}

	private val _elements = combine(
		masterDataRepository.classes,
		masterDataRepository.teachers,
		masterDataRepository.subjects,
		masterDataRepository.rooms
	) { classes, teachers, subjects, rooms ->
		mapOf(
			ElementType.CLASS to classes,
			ElementType.TEACHER to teachers,
			ElementType.SUBJECT to subjects,
			ElementType.ROOM to rooms
		)
	}
	val elements: StateFlow<Map<ElementType, List<ElementEntity>>> = _elements.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = emptyMap()
	)

	val globalRepository = globalSettingsRepository

	private val _contributors = MutableStateFlow<List<GitHubUser>>(emptyList())
	val contributors: StateFlow<List<GitHubUser>> = _contributors

	private val _contributorsError = MutableStateFlow<Throwable?>(null)
	val contributorsError: StateFlow<Throwable?> = _contributorsError

	fun resetColors() = viewModelScope.launch {
		userSettingsRepository.updateSettings {
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

	fun currentUserId() = userRepository.currentUser?.id

	suspend fun loadContributors() {
		_contributorsError.value = null

		try {
			_contributors.value = httpClient.get("$URL_GITHUB_REPOSITORY_API/contributors").body()
		} catch (e: Exception) {
			_contributorsError.value = e
		}
	}
}
