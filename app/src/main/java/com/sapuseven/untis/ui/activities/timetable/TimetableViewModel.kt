package com.sapuseven.untis.ui.activities.timetable

import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sapuseven.compose.protostore.ui.preferences.convertRangeToPair
import com.sapuseven.untis.R
import com.sapuseven.untis.api.client.TimetableApi
import com.sapuseven.untis.api.exception.UntisApiException
import com.sapuseven.untis.components.ElementPicker
import com.sapuseven.untis.components.UserManager
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.mappers.TimetableMapper
import com.sapuseven.untis.modules.ThemeManager
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.ui.activities.settings.SettingsRepository
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.ui.weekview.Event
import com.sapuseven.untis.ui.weekview.WeekViewHour
import com.sapuseven.untis.ui.weekview.startDateForPageIndex
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

@HiltViewModel(assistedFactory = TimetableViewModel.Factory::class)
class TimetableViewModel @AssistedInject constructor(
	private val navigator: AppNavigator,
	private val themeManager: ThemeManager,
	internal val userManager: UserManager,
	private val userScopeManager: UserScopeManager,
	private val userDao: UserDao,
	private val api: TimetableApi,
	private val repositoryFactory: SettingsRepository.Factory,
	private val timetableMapperFactory: TimetableMapper.Factory,
	@Assisted val colorScheme: ColorScheme,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	@AssistedFactory
	interface Factory {
		fun create(colorScheme: ColorScheme): TimetableViewModel
	}

	private val timetableMapper = timetableMapperFactory.create(colorScheme)
	private val repository = repositoryFactory.create(colorScheme)

	val args = savedStateHandle.toRoute<AppRoutes.Timetable>()

	private val _currentUserSettings =
		MutableStateFlow<UserSettings>(repository.getSettingsDefaults())
	val currentUserSettings: StateFlow<UserSettings> = _currentUserSettings

	var profileManagementDialog by mutableStateOf(false)
	var feedbackDialog by mutableStateOf(false)

	var loading by mutableStateOf(true)

	private val _debugColor = MutableStateFlow(0x0)
	val debugColor: StateFlow<Int> = _debugColor

	private val _needsPersonalTimetable = MutableStateFlow(false)
	val needsPersonalTimetable: StateFlow<Boolean> = _needsPersonalTimetable

	private val _hourList = MutableStateFlow<List<WeekViewHour>>(emptyList())
	val hourList: StateFlow<List<WeekViewHour>> = _hourList

	private val _events = MutableStateFlow<Map<LocalDate, List<Event>>>(emptyMap())
	val events: StateFlow<Map<LocalDate, List<Event>>> = _events

	val currentUser: User = userScopeManager.user

	val allUsersState: StateFlow<List<User>> = userManager.allUsersState

	val elementPicker: ElementPicker
		get() = ElementPicker(userScopeManager.user, userDao)

	init {
		viewModelScope.launch {
			repository.getSettings().collect { userSettings ->
				// All properties that are based on preferences are set here
				_currentUserSettings.value = userSettings
				_needsPersonalTimetable.value = userSettings.timetablePersonalTimetable.isBlank()
				_hourList.value = buildHourList(
					currentUser,
					userSettings.timetableRange.convertRangeToPair(),
					userSettings.timetableRangeIndexReset
				)
				_debugColor.value = userSettings.backgroundRegular
			}
		}

		viewModelScope.launch {
			debugColor.collect {
				// TODO: For some reason, the elements are not really getting updated. the view doesn't recompose when it changes, and even when the color-function changes the text, it doesn't update
				emitEvents(_events.value.mapValues { timetableMapper.colorWeekViewTimetableEvents(it.value) })
			}
		}
	}

	fun switchUser(user: User) {
		userManager.switchUser(user)
		/*navigator.navigate(AppRoutes.Timetable(user.id)) {
			popUpTo(0) // Pop all previous routes
		}*/
	}

	fun editUser(user: User?) {
		navigator.navigate(AppRoutes.LoginDataInput(userId = user?.id ?: -1))
	}

	fun editUsers() {
		profileManagementDialog = true
	}

	fun toggleTheme() {
		/*user.value?.id?.let {
			themeManager.toggleTheme(it)
		}*/
	}

	suspend fun onPageChange(pageOffset: Int = 0) {
		viewModelScope.launch {
			loading = true
			((pageOffset - 1)..(pageOffset + 1)).map {
				async {
					val startDate = startDateForPageIndex(it.toLong())
					Log.d(
						"WeekView",
						"Items available for $startDate: ${_events.value.contains(startDate)}"
					)
					if (!_events.value.contains(startDate))
						loadEvents(startDate)?.let { events ->
							emitEvents(mapOf(startDate to timetableMapper.colorWeekViewTimetableEvents(events)))
						}
				}
			}.awaitAll()
			loading = false
		}
	}

	suspend fun onPageReload(pageOffset: Int) {
		loading = true
		val startDate = startDateForPageIndex(pageOffset.toLong())
		loadEvents(startDate)?.let { events ->
			emitEvents(mapOf(startDate to timetableMapper.colorWeekViewTimetableEvents(events)))
		}
		loading = false
	}

	private suspend fun loadEvents(startDate: LocalDate): List<Event>? {
		try {
			return timetableMapper.mapTimetablePeriodsToWeekViewEvents(
				api.loadTimetable(
					id = 0,
					type = TimetableDatabaseInterface.Type.STUDENT.name,
					startDate = startDate,
					endDate = startDate.plusDays(5 /*TODO*/),
					masterDataTimestamp = currentUser.masterDataTimestamp,
					apiUrl = currentUser.apiUrl,
					user = currentUser.user,
					key = currentUser.key
				).timetable.periods,
				TimetableDatabaseInterface.Type.STUDENT
			)
		} catch (e: UntisApiException) {
			Log.e("TimetableViewModel", "Failed to load timetable due to API error", e)
		} catch (e: Exception) {
			Log.e("TimetableViewModel", "Failed to load timetable due to other error", e)
		}
		return null
	}

	private suspend fun emitEvents(events: Map<LocalDate, List<Event>>) {
		val newEvents = _events.value.toMutableMap()
		events.forEach { (date, events) ->
			newEvents[date] = events
		}
		_events.emit(newEvents.toMap())
	}

	private fun groupEventsByDate(events: List<Event>): Map<LocalDate, List<Event>> {
		val groupedEvents = mutableMapOf<LocalDate, MutableList<Event>>()

		for (event in events) {
			val eventDate = event.start.toLocalDate()
			groupedEvents.computeIfAbsent(eventDate) { mutableListOf() }.add(event)
		}

		// Convert the map to an immutable version if needed
		return groupedEvents.mapValues { it.value.toList() }
	}

	fun buildHourList(
		user: User, range: Pair<Int, Int>?, rangeIndexReset: Boolean
	): List<WeekViewHour> {
		val hourList = mutableListOf<WeekViewHour>()

		user.timeGrid.days.maxByOrNull { it.units.size }?.units?.forEachIndexed { index, hour ->
			// Check if outside configured range
			if (range?.let { index < it.first - 1 || index >= it.second } == true) return@forEachIndexed

			// If label is empty, fill it according to preferences
			val label = hour.label.ifEmpty {
				if (rangeIndexReset) (index + 1).toString()
				else ((range?.first ?: 1) + index).toString()
			}

			hourList.add(WeekViewHour(hour.startTime, hour.endTime, label))
		}

		return hourList
	}

	val onAnonymousSettingsClick = {
		navigator.navigate(AppRoutes.Settings.Timetable(highlightTitle = R.string.preference_timetable_personal_timetable))
	}

	// TODO
	@Composable
	fun lastRefreshText() = stringResource(
		id = R.string.main_last_refreshed,
		/*if (weekViewRefreshTimestamps[weekViewPage] ?: 0L > 0L) formatTimeDiff(Instant.now().millis - weekViewRefreshTimestamps[weekViewPage]!!)
		else*/ stringResource(id = R.string.main_last_refreshed_never)
	)

	fun showFeedback() {
		feedbackDialog = true
	}

	fun debugAction() = viewModelScope.launch {
		repository.updateSettings {
			backgroundRegular = ((Math.random() * 0xFFFFFF).toInt()) or (0xFF shl 24);
		}
	}
}
