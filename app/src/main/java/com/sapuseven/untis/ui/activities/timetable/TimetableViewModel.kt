package com.sapuseven.untis.ui.activities.timetable

import android.content.Context
import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sapuseven.compose.protostore.ui.preferences.convertRangeToPair
import com.sapuseven.untis.BuildConfig
import com.sapuseven.untis.R
import com.sapuseven.untis.api.exception.UntisApiException
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.Period
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.components.ElementPicker
import com.sapuseven.untis.components.UserManager
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.data.repository.TimetableRepository
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.mappers.TimetableMapper
import com.sapuseven.untis.models.PeriodItem
import com.sapuseven.untis.modules.ThemeManager
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.ui.activities.settings.GlobalSettingsRepository
import com.sapuseven.untis.ui.activities.settings.UserSettingsRepository
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.ui.preferences.decodeStoredTimetableValue
import com.sapuseven.untis.ui.weekview.Event
import com.sapuseven.untis.ui.weekview.WeekViewColorScheme
import com.sapuseven.untis.ui.weekview.WeekViewHour
import com.sapuseven.untis.ui.weekview.startDateForPageIndex
import crocodile8.universal_cache.CachedSourceResult
import crocodile8.universal_cache.FromCache
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@HiltViewModel(assistedFactory = TimetableViewModel.Factory::class)
class TimetableViewModel @AssistedInject constructor(
	private val navigator: AppNavigator,
	private val themeManager: ThemeManager,
	internal val userManager: UserManager,
	private val userScopeManager: UserScopeManager,
	private val userDao: UserDao,
	private val userSettingsRepositoryFactory: UserSettingsRepository.Factory,
	private val timetableMapperFactory: TimetableMapper.Factory,
	private val timetableRepositoryFactory: TimetableRepository.Factory,
	internal val globalSettingsRepository: GlobalSettingsRepository,
	@Assisted private val colorScheme: ColorScheme,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	@AssistedFactory
	interface Factory {
		fun create(colorScheme: ColorScheme): TimetableViewModel
	}

	private val timetableMapper = timetableMapperFactory.create(colorScheme)
	private val timetableRepository = timetableRepositoryFactory.create(colorScheme)
	private val userSettingsRepository = userSettingsRepositoryFactory.create(colorScheme)

	val args = savedStateHandle.toRoute<AppRoutes.Timetable>()

	private val _currentUserSettings =
		MutableStateFlow<UserSettings>(userSettingsRepository.getSettingsDefaults())
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

	private val _currentElement = MutableStateFlow<PeriodElement?>(null)
	val currentElement: StateFlow<PeriodElement?> = _currentElement

	private val _events = MutableStateFlow<Map<LocalDate, List<Event<PeriodItem>>>>(emptyMap())
	val events: StateFlow<Map<LocalDate, List<Event<PeriodItem>>>> = _events

	private val _lastRefresh = MutableStateFlow<Instant?>(null)
	val lastRefresh: StateFlow<Instant?> = _lastRefresh

	private val _weekViewColorScheme = MutableStateFlow<WeekViewColorScheme>(WeekViewColorScheme.default(colorScheme))
	val weekViewColorScheme: StateFlow<WeekViewColorScheme> = _weekViewColorScheme

	val currentUser: User = userScopeManager.user

	val allUsersState: StateFlow<List<User>> = userManager.allUsersState

	val elementPicker: ElementPicker
		get() = ElementPicker(userScopeManager.user, userDao)

	private val loadingExceptionHandler: suspend FlowCollector<*>.(Throwable) -> Unit = { throwable ->
		val message = if (throwable is UntisApiException) "API error" else "other error"
		Log.e("TimetableViewModel", "Failed to load timetable due to $message", throwable)
	}

	init {
		viewModelScope.launch {
			userSettingsRepository.getSettings().collect { userSettings ->
				// All properties that are based on preferences are set here
				_currentUserSettings.value = userSettings
				decodeStoredTimetableValue(userSettings.timetablePersonalTimetable)?.let {
					_needsPersonalTimetable.emit(false)
					_currentElement.emit(it)
				} ?: _needsPersonalTimetable.emit(true)
				_hourList.value = buildHourList(
					user = currentUser,
					range = userSettings.timetableRange.convertRangeToPair(),
					rangeIndexReset = userSettings.timetableRangeIndexReset
				)
				_debugColor.value = userSettings.backgroundRegular
				_weekViewColorScheme.value = WeekViewColorScheme(
					dividerColor = colorScheme.outline,
					pastBackgroundColor = Color(userSettings.backgroundPast),
					futureBackgroundColor = Color(userSettings.backgroundFuture),
					indicatorColor = Color(userSettings.marker),
				)
			}
		}

		viewModelScope.launch {
			debugColor.collect {
				_events.update {
					it.mapValues { timetableMapper.colorWeekViewTimetableEvents(it.value).toList() }
				}
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
			((pageOffset - 1)..(pageOffset + 1)).map { targetPage ->
				async {
					val startDate = startDateForPageIndex(targetPage.toLong())
					loadEvents(
						startDate,
						// TODO: Right now this is a workaround to show the "last refresh" text when changing pages,
						//  since it isn't stored after emitting the events.
						//  For that reason the cache is queried more often than necessary.
						if (_events.value.contains(startDate)) FromCache.ONLY else FromCache.CACHED_THEN_LOAD
					)
						.catch(loadingExceptionHandler)
						.collect {
							val events =
								timetableMapper.mapTimetablePeriodsToWeekViewEvents(it.value, ElementType.STUDENT)
							val refreshTimestamp = it.originTimeStamp?.let { Instant.ofEpochMilli(it) } ?: Instant.now()
							emitEvents(mapOf(startDate to timetableMapper.colorWeekViewTimetableEvents(events)))
							if (targetPage == pageOffset)
								_lastRefresh.emit(refreshTimestamp)
						}
				}
			}.awaitAll()
			loading = false
		}
	}

	suspend fun onPageReload(pageOffset: Int) {
		val startDate = startDateForPageIndex(pageOffset.toLong())
		loadEvents(startDate, FromCache.NEVER)
			.catch(loadingExceptionHandler)
			.collect {
				val events = timetableMapper.mapTimetablePeriodsToWeekViewEvents(it.value, ElementType.STUDENT)
				val refreshTimestamp = it.originTimeStamp?.let { Instant.ofEpochMilli(it) } ?: Instant.now()
				emitEvents(mapOf(startDate to timetableMapper.colorWeekViewTimetableEvents(events)))
				_lastRefresh.emit(refreshTimestamp)
			}
	}

	private suspend fun loadEvents(startDate: LocalDate, fromCache: FromCache): Flow<CachedSourceResult<List<Period>>> {
		return timetableRepository.timetableSource().getRaw(
			params = TimetableRepository.TimetableParams(
				elementId = 0,
				elementType = ElementType.STUDENT,
				startDate = startDate,
			),
			additionalKey = currentUser.id,
			fromCache = fromCache
		)
	}

	private suspend fun emitEvents(events: Map<LocalDate, List<Event<PeriodItem>>>) {
		_events.update {
			val newEvents = it.toMutableMap()
			events.forEach { (date, events) ->
				newEvents[date] = events
			}
			newEvents.toMap()
		}
	}

	private fun groupEventsByDate(events: List<Event<PeriodItem>>): Map<LocalDate, List<Event<PeriodItem>>> {
		val groupedEvents = mutableMapOf<LocalDate, MutableList<Event<PeriodItem>>>()

		for (event in events) {
			val eventDate = event.start.toLocalDate()
			groupedEvents.computeIfAbsent(eventDate) { mutableListOf() }.add(event)
		}

		// Convert the map to an immutable version if needed
		return groupedEvents.mapValues { it.value.toList() }
	}

	private fun buildHourList(
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

	fun showFeedback() {
		feedbackDialog = true
	}

	fun debugAction() = viewModelScope.launch {
		userSettingsRepository.updateSettings {
			backgroundRegular = ((Math.random() * 0xFFFFFF).toInt()) or (0xFF shl 24);
		}
	}

	fun getTitle(context: Context) = _currentElement.value?.let {
		it.type.name + " " + it.id
	} ?: currentUser.getDisplayedName(context) + (if (BuildConfig.DEBUG) " (${currentUser.id})" else "")

	fun showElement(element: PeriodElement?) {
		TODO("Not yet implemented")
	}
}
