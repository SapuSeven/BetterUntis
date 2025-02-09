package com.sapuseven.untis.ui.pages.timetable

import android.content.Context
import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sapuseven.compose.protostore.ui.preferences.convertRangeToPair
import com.sapuseven.untis.R
import com.sapuseven.untis.api.exception.UntisApiException
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.Period
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.components.BuildConfigFieldsProvider
import com.sapuseven.untis.components.ElementPicker
import com.sapuseven.untis.components.UserManager
import com.sapuseven.untis.data.database.entities.User
import com.sapuseven.untis.data.database.entities.UserDao
import com.sapuseven.untis.data.repository.MasterDataRepository
import com.sapuseven.untis.data.repository.TimetableRepository
import com.sapuseven.untis.mappers.TimetableMapper
import com.sapuseven.untis.models.PeriodItem
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.ui.navigation.AppNavigator
import com.sapuseven.untis.ui.navigation.AppRoutes
import com.sapuseven.untis.ui.pages.settings.GlobalSettingsRepository
import com.sapuseven.untis.ui.pages.settings.UserSettingsRepository
import com.sapuseven.untis.ui.preferences.decodeStoredTimetableValue
import com.sapuseven.untis.ui.weekview.Event
import com.sapuseven.untis.ui.weekview.WeekViewColorScheme
import com.sapuseven.untis.ui.weekview.WeekViewEventStyle
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

@HiltViewModel(assistedFactory = TimetableViewModel.Factory::class)
class TimetableViewModel @AssistedInject constructor(
	private val navigator: AppNavigator,
	internal val userManager: UserManager,
	private val userScopeManager: UserScopeManager,
	private val userDao: UserDao,
	internal val timetableRepository: TimetableRepository,
	internal val masterDataRepository: MasterDataRepository,
	internal val globalSettingsRepository: GlobalSettingsRepository,
	internal val clock: Clock,
	@Assisted private val colorScheme: ColorScheme,
	@Assisted private val typography: Typography,
	buildConfigFieldsProvider: BuildConfigFieldsProvider,
	userSettingsRepositoryFactory: UserSettingsRepository.Factory,
	timetableMapperFactory: TimetableMapper.Factory,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	@AssistedFactory
	interface Factory {
		fun create(colorScheme: ColorScheme, typography: Typography): TimetableViewModel
	}

	private val timetableMapper = timetableMapperFactory.create(colorScheme)
	private val userSettingsRepository = userSettingsRepositoryFactory.create(colorScheme)

	private val args = savedStateHandle.toRoute<AppRoutes.Timetable>()

	val requestedElement = args.getElement()

	var profileManagementDialog by mutableStateOf(false)
	var timetableItemDetailsDialog by mutableStateOf<Pair<List<Event<PeriodItem>>, Int>?>(null)
	var feedbackDialog by mutableStateOf(false)
	var loading by mutableStateOf(true)

	val currentUser: User = userScopeManager.user
	val allUsersState: StateFlow<List<User>> = userManager.allUsersState

	val isDebug = buildConfigFieldsProvider.get().isDebug

	private val _personalElement = MutableStateFlow<PeriodElement?>(null)

	private val _needsPersonalTimetable = MutableStateFlow(false)
	val needsPersonalTimetable: StateFlow<Boolean> = _needsPersonalTimetable

	private val _hourList = MutableStateFlow<List<WeekViewHour>>(emptyList())
	val hourList: StateFlow<List<WeekViewHour>> = _hourList

	private val _events = MutableStateFlow<Map<LocalDate, List<Event<PeriodItem>>>>(emptyMap())
	val events: StateFlow<Map<LocalDate, List<Event<PeriodItem>>>> = _events

	private val _lastRefresh = MutableStateFlow<Instant?>(null)
	val lastRefresh: StateFlow<Instant?> = _lastRefresh

	private val _weekViewColorScheme = MutableStateFlow(WeekViewColorScheme.default(colorScheme))
	val weekViewColorScheme: StateFlow<WeekViewColorScheme> = _weekViewColorScheme

	private val _weekViewScale = MutableStateFlow(1f)
	val weekViewScale: StateFlow<Float> = _weekViewScale

	private val _weekViewZoomEnabled = MutableStateFlow(true)
	val weekViewZoomEnabled: StateFlow<Boolean> = _weekViewZoomEnabled

	private val _weekViewEventStyle = MutableStateFlow(WeekViewEventStyle.default(typography))
	val weekViewEventStyle: StateFlow<WeekViewEventStyle> = _weekViewEventStyle

	private var currentPage = 0

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
				decodeStoredTimetableValue(userSettings.timetablePersonalTimetable)?.let {
					_personalElement.value = it
					_needsPersonalTimetable.emit(false)
					//_currentElement.update { prev -> prev ?: it } // Update only if null
				} ?: run {
					_personalElement.value = currentUser.userData.elemType?.let {
						PeriodElement(it, currentUser.userData.elemId)
					}
					_needsPersonalTimetable.emit(requestedElement == null && _personalElement.value == null)
				}
				_hourList.value = buildHourList(
					user = currentUser,
					range = userSettings.timetableRange.convertRangeToPair(),
					rangeIndexReset = userSettings.timetableRangeIndexReset
				)
				_weekViewColorScheme.value = WeekViewColorScheme(
					dividerColor = colorScheme.outline,
					pastBackgroundColor = Color(userSettings.backgroundPast),
					futureBackgroundColor = Color(userSettings.backgroundFuture),
					indicatorColor = Color(userSettings.marker),
				)
				_weekViewEventStyle.value = WeekViewEventStyle(
					padding = userSettings.timetableItemPadding,
					cornerRadius = userSettings.timetableItemCornerRadius,
					lessonNameStyle = typography.bodyLarge.copy(
						fontSize = userSettings.timetableLessonNameFontSize.sp,
						fontWeight = if (userSettings.timetableBoldLessonName) FontWeight.Bold else FontWeight.Normal
					),
					lessonInfoStyle = typography.bodySmall.copy(fontSize = userSettings.timetableLessonInfoFontSize.sp),
					lessonInfoCentered = userSettings.timetableCenteredLessonInfo,
				)
				_weekViewScale.value = userSettings.timetableZoomLevel
				_weekViewZoomEnabled.value = userSettings.timetableZoomEnabled
			}
		}
	}

	fun switchUser(user: User) {
		userManager.switchUser(user)
	}

	fun editUser(user: User?) {
		navigator.navigate(AppRoutes.LoginDataInput(userId = user?.id ?: -1))
	}

	fun editUsers() {
		profileManagementDialog = true
	}

	fun onPageChange(pageOffset: Int = currentPage) {
		currentPage = pageOffset
		viewModelScope.launch {
			loading = true
			((pageOffset - 1)..(pageOffset + 1)).map { targetPage ->
				async {
					val startDate = startDateForPageIndex(targetPage.toLong())
					loadEvents(
						startDate,
						// Note: Right now this is a workaround to show the "last refresh" text when changing pages,
						//  since it isn't stored after emitting the events.
						//  For that reason the cache is queried more often than necessary.
						if (_events.value.contains(startDate)) FromCache.ONLY else FromCache.CACHED_THEN_LOAD
					)
						.catch(loadingExceptionHandler)
						.collect { result ->
							val events =
								timetableMapper.mapTimetablePeriodsToWeekViewEvents(result.value, ElementType.STUDENT)
							val refreshTimestamp =
								result.originTimeStamp?.let { Instant.ofEpochMilli(it) } ?: Instant.now()
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
			.collect { result ->
				val events = timetableMapper.mapTimetablePeriodsToWeekViewEvents(result.value, ElementType.STUDENT)
				val refreshTimestamp = result.originTimeStamp?.let { Instant.ofEpochMilli(it) } ?: Instant.now()
				emitEvents(mapOf(startDate to timetableMapper.colorWeekViewTimetableEvents(events)))
				_lastRefresh.emit(refreshTimestamp)
			}
	}

	fun onItemClick(itemsWithIndex: Pair<List<Event<PeriodItem>>, Int>) {
		timetableItemDetailsDialog = itemsWithIndex
	}

	suspend fun onZoom(zoomLevel: Float) {
		userSettingsRepository.updateSettings { timetableZoomLevel = zoomLevel }
	}

	private suspend fun loadEvents(startDate: LocalDate, fromCache: FromCache): Flow<CachedSourceResult<List<Period>>> {
		// Load the requested element (nav args) or the personal element
		val elementToLoad = requestedElement ?: _personalElement
			.combine(_needsPersonalTimetable) { element, _ -> element } // Emit a value if _personalElement or _needsPersonalTimetable changes
			.first { it != null || _needsPersonalTimetable.value } // Take the first non-null _personalElement or null if _needsPersonalTimetable

		return elementToLoad?.let {
			timetableRepository.timetableSource().getRaw(
				params = TimetableRepository.TimetableParams(
					elementId = it.id,
					elementType = it.type,
					startDate = startDate,
				),
				fromCache = fromCache,
				additionalKey = currentUser
			)
		} ?: emptyFlow()
	}

	private fun emitEvents(events: Map<LocalDate, List<Event<PeriodItem>>>) {
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

	// TODO: Extract to usecase
	private fun buildHourList(
		user: User, range: Pair<Int, Int>?, rangeIndexReset: Boolean
	): List<WeekViewHour> {
		val hourList = mutableListOf<WeekViewHour>()

		user.timeGrid.days.maxByOrNull { it.units.size }?.units?.forEachIndexed { index, hour ->
			// Check if outside configured range
			if (range?.let { index < it.first - 1 || index >= it.second } == true) return@forEachIndexed

			// If label is empty, fill it according to preferences
			val label = if (rangeIndexReset) {
				(index + 2 - (range?.first ?: 1)).toString()
			} else {
				hour.label.ifEmpty { (index + 1).toString() }
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

	fun getTitle(context: Context) = requestedElement?.let {
		if (it == _personalElement.value) null // Use Profile name for personal timetable
		else masterDataRepository.getLongName(it)
	}
		?: (currentUser.getDisplayedName(context) + (if (isDebug) " (${currentUser.id})" else ""))

	fun showElement(element: PeriodElement?) {
		navigator.navigate(AppRoutes.Timetable(element))
	}
}
