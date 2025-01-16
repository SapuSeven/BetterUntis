package com.sapuseven.untis.ui.pages.roomfinder

import androidx.lifecycle.ViewModel
import com.sapuseven.untis.ui.navigation.AppNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class RoomFinderViewModel @Inject constructor(
	private val navigator: AppNavigator,
) : ViewModel() {
	private val _showElementPicker = MutableStateFlow(false)
	val showElementPicker: StateFlow<Boolean> = _showElementPicker

	fun goBack() {
		navigator.popBackStack()
	}

	fun onAddButtonClick() {
		_showElementPicker.value = true
	}

	/*val isRoomListEmpty: Boolean
		get() = roomList.isEmpty()

	val shouldShowElementPicker: Boolean
		get() = showElementPicker.value

	val shouldShowDeleteItem: Boolean
		get() = deleteItem != DELETE_ITEM_NONE

	val currentDeleteItem: RoomStatusData?
		get() = roomList.find { it.periodElement.id == deleteItem }

	val currentUnit: Triple<Day, Int, com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit>?
		get() = getUnitFromIndex(user, currentHourIndex)

	val currentHourIndex: Int
		get() = hourIndex.value

	val hourIndexCanDecrease: Boolean
		get() = currentHourIndex > 0

	val hourIndexCanIncrease: Boolean
		get() = currentHourIndex < maxHourIndex

	val sortedRoomList: List<RoomStatusData>
		get() = roomList.sortedWith(
			compareByDescending<RoomStatusData> {
				it.getState(currentHourIndex)
			}.thenBy { it.name }
		)

	private var deleteItem by mutableStateOf(DELETE_ITEM_NONE)

	private val maxHourIndex = calculateMaxHourIndex(user)

	private val roomList = mutableStateListOf(
		*roomFinderDatabase.getAllRooms().map {
			RoomStatusData(
				PeriodElement(
					ElementType.ROOM, it.id.toLong(), it.id.toLong()
				),
				timetableDatabaseInterface,
				it.states
			)
		}.toTypedArray()
	)

	private fun calculateMaxHourIndex(user: User): Int {
		var maxHourIndex = -1 // maxIndex = -1 + length
		user.timeGrid.days.forEach { day ->
			maxHourIndex += day.units.size
		}
		return maxHourIndex
	}

	@Throws(TimetableLoader.TimetableLoaderException::class)
	private suspend fun loadStates(
		user: User,
		roomId: Long,
		proxyHost: String?
	): List<Boolean> {
		val states = mutableListOf<Boolean>()

		/*val startDate = UntisDate.fromLocalDate(
			LocalDate.now().withDayOfWeek(user.timeGrid.days.first().day)
		)
		val endDate = UntisDate.fromLocalDate(
			LocalDate.now().withDayOfWeek(user.timeGrid.days.last().day)
		)*/

		// Dummy Data:
		/*delay(1000 + nextLong(0, 2000))
		for (i in 0..10)
			states.add(nextBoolean())*/

		/*TimetableLoader(
			context = WeakReference(contextActivity),
			user = user,
			timetableDatabaseInterface = timetableDatabaseInterface
		).loadAsync(
			TimetableLoader.TimetableLoaderTarget(
				startDate,
				endDate,
				roomId,
				TimetableDatabaseInterface.Type.ROOM.name
			),
			proxyHost,
			loadFromServer = true
		) { timetableItems ->
			val loadedStates = mutableListOf<Boolean>()
			user.timeGrid.days.forEach { day ->
				val dayDateTime =
					DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH)
						.parseDateTime(day.day)

				day.units.forEach { unit ->
					val unitStartDateTime = unit.startTime
					val unitEndDateTime = unit.endTime

					var occupied = false

					timetableItems.items.forEach allItems@{ item ->
						if (item.startDateTime.dayOfWeek == dayDateTime.dayOfWeek)
							if (item.startDateTime.millisOfDay <= LocalTime(unitEndDateTime).millisOfDay
								&& item.endDateTime.millisOfDay >= LocalTime(unitStartDateTime).millisOfDay
							) {
								occupied = true
								return@allItems
							}
					}

					loadedStates.add(occupied)
				}
			}

			states.addAll(loadedStates.toList())
		}*/

		return states.toList()
	}

	data class RoomStatusData(
		val periodElement: PeriodElement,
		val timetableDatabaseInterface: TimetableDatabaseInterface? = null,
		val states: List<Boolean>? = null,
		val name: String = "",//timetableDatabaseInterface?.getShortName(periodElement) ?: "",
		val errorMessage: String? = null,
		var isLoading: Boolean = states == null,
		var isError: Boolean = states?.isEmpty() ?: false
	) {
		fun getState(hourIndex: Int): Int {
			return states?.let { states ->
				if (isLoading)
					return@let ROOM_STATE_LOADING
				var i = 0
				var hours = 0
				while (hourIndex + i < states.size && !states[hourIndex + i]) {
					hours++
					i++
				}
				return@let hours
			} ?: ROOM_STATE_LOADING
		}
	}

	/**
	 * @return A triple of the day, the unit index of day (1-indexed) and the unit corresponding to the provided hour index.
	 */
	private fun getUnitFromIndex(
		user: User,
		index: Int
	): Triple<Day, Int, com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit>? {
		var indexCounter = index
		user.timeGrid.days.forEach { day ->
			if (indexCounter >= day.units.size)
				indexCounter -= day.units.size
			else
				return Triple(day, indexCounter + 1, day.units[indexCounter])
		}
		return null
	}

	fun translateDay(day: DayOfWeek): String {
		return day.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
	}

	fun deleteItem(item: RoomStatusData) {
		if (roomFinderDatabase.deleteRoom(item.periodElement.id.toInt()))
			roomList.remove(item)
	}

	fun onRoomListItemClick(item: RoomStatusData) {
		contextActivity.setResult(
			Activity.RESULT_OK, Intent().putExtra(
				EXTRA_STRING_PERIOD_ELEMENT,
				Json.encodeToString(PeriodElement.serializer(), item.periodElement)
			)
		)
		contextActivity.finish()
	}

	fun onRoomListItemDeleteClick(item: RoomStatusData) {
		deleteItem = item.periodElement.id
	}

	fun onAddButtonClick() {
		showElementPicker.value = true
	}

	fun onBackClick() {
		contextActivity.finish()
	}

	fun onIncreaseHourIndex() {
		if (hourIndexCanIncrease)
			hourIndex.value++
	}

	fun onDecreaseHourIndex() {
		if (hourIndexCanDecrease)
			hourIndex.value--
	}

	fun onResetHourIndex() {
		hourIndex.value = calculateCurrentHourIndex(user)
	}

	fun onDeleteItemDialogDismiss() {
		deleteItem = DELETE_ITEM_NONE
	}

	fun onElementPickerDismiss() {
		showElementPicker.value = false
	}

	fun onElementPickerSelect(selectedItems: List<PeriodElement>) {
		showElementPicker.value = false

		selectedItems
			.filter { roomList.find { existing -> existing.periodElement.id == it.id } == null }
			.forEach { periodElement ->
				scope.launch {
					val item = RoomStatusData(
						periodElement,
						timetableDatabaseInterface
					)
					roomList.add(item)
					val (states, error) = try {
						loadStates(
							user,
							periodElement.id,
							preferences.proxyHost.getValue()
						) to null
					} catch (e: TimetableLoader.TimetableLoaderException) {
						emptyList<Boolean>() to ErrorMessageDictionary.getErrorMessage(
							contextActivity.resources,
							e.untisErrorCode,
							e.untisErrorMessage
						)
					}

					roomFinderDatabase.addRoom(
						RoomFinderItem(
							periodElement.id.toInt(),
							states
						)
					)

					roomList.remove(item)
					roomList.add(
						RoomStatusData(
							periodElement = periodElement,
							timetableDatabaseInterface = timetableDatabaseInterface,
							states = states,
							errorMessage = error
						)
					)
				}
			}
	}*/
}
