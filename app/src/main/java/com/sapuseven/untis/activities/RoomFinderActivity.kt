package com.sapuseven.untis.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.MainActivity.Companion.EXTRA_STRING_PERIOD_ELEMENT
import com.sapuseven.untis.activities.RoomFinderState.Companion.ROOM_STATE_FREE
import com.sapuseven.untis.activities.RoomFinderState.Companion.ROOM_STATE_OCCUPIED
import com.sapuseven.untis.data.databases.RoomFinderDatabase
import com.sapuseven.untis.data.databases.LegacyUserDatabase
import com.sapuseven.untis.helpers.ErrorMessageDictionary
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableLoader
import com.sapuseven.untis.models.RoomFinderItem
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.masterdata.timegrid.Day
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.preferences.DataStorePreferences
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationEnter
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationExit
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.dialogs.ElementPickerDialogFullscreen
import com.sapuseven.untis.ui.functional.bottomInsets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.lang.ref.WeakReference
import java.util.*

class RoomFinderActivity : BaseComposeActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			AppTheme(navBarInset = false) {
				withUser { user ->
					val state = rememberRoomFinderState(
						user,
						timetableDatabaseInterface,
						dataStorePreferences,
						this
					)
					RoomFinder(state)
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RoomFinder(state: RoomFinderState) {
	AppScaffold(
		modifier = Modifier.bottomInsets(),
		topBar = {
			CenterAlignedTopAppBar(
				title = {
					Text(stringResource(id = R.string.activity_title_free_rooms))
				},
				navigationIcon = {
					IconButton(onClick = { state.onBackClick() }) {
						Icon(
							imageVector = Icons.Outlined.ArrowBack,
							contentDescription = stringResource(id = R.string.all_back)
						)
					}
				},
				actions = {
					IconButton(onClick = { state.onAddButtonClick() }) {
						Icon(
							imageVector = Icons.Outlined.Add,
							contentDescription = stringResource(id = R.string.all_add)
						)
					}
				}
			)
		}
	) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(innerPadding)
				.fillMaxSize()
		) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center,
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
			) {
				LazyColumn(
					Modifier
						.fillMaxWidth()
						.weight(1f)
				) {
					items(
						state.sortedRoomList,
						key = { it.periodElement.id }
					) {
						RoomListItem(
							item = it,
							hourIndex = state.currentHourIndex,
							onDelete = { state.onRoomListItemDeleteClick(it) },
							modifier = Modifier
								.animateItemPlacement()
								.clickable { state.onRoomListItemClick(it) }
						)
					}
				}

				if (state.isRoomListEmpty)
					RoomFinderListEmpty(
						modifier = Modifier
							.align(Alignment.CenterHorizontally)
							.weight(1f)
					)
				else
					RoomFinderHourSelector(state)
			}
		}
	}

	AnimatedVisibility(
		visible = state.shouldShowElementPicker,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		ElementPickerDialogFullscreen(
			title = { Text(stringResource(id = R.string.all_add)) }, // TODO: Proper string resource
			multiSelect = true,
			hideTypeSelection = true,
			initialType = TimetableDatabaseInterface.Type.ROOM,
			timetableDatabaseInterface = state.timetableDatabaseInterface,
			onDismiss = { state.onElementPickerDismiss() },
			onMultiSelect = { state.onElementPickerSelect(it) }
		)
	}

	if (state.shouldShowDeleteItem) {
		state.currentDeleteItem?.let { item ->
			AlertDialog(
				onDismissRequest = {
					state.onDeleteItemDialogDismiss()
				},
				title = {
					Text(
						stringResource(
							id = R.string.roomfinder_dialog_itemdelete_title,
							item.name
						)
					)
				},
				text = {
					Text(stringResource(id = R.string.roomfinder_dialog_itemdelete_text))
				},
				confirmButton = {
					TextButton(
						onClick = {
							state.onDeleteItemDialogDismiss()
							state.deleteItem(item)
						}) {
						Text(stringResource(id = R.string.all_yes))
					}
				},
				dismissButton = {
					TextButton(
						onClick = {
							state.onDeleteItemDialogDismiss()
						}) {
						Text(stringResource(id = R.string.all_no))
					}
				}
			)
		}
	}
}

@Composable
fun RoomFinderListEmpty(modifier: Modifier = Modifier) {
	val annotatedString = buildAnnotatedString {
		val text = stringResource(R.string.roomfinder_no_rooms)
		append(text.substring(0, text.indexOf("+")))
		appendInlineContent(id = "add")
		append(text.substring(text.indexOf("+") + 1))
	}

	val inlineContentMap = mapOf(
		"add" to InlineTextContent(
			Placeholder(
				MaterialTheme.typography.bodyLarge.fontSize,
				MaterialTheme.typography.bodyLarge.fontSize,
				PlaceholderVerticalAlign.TextCenter
			)
		) {
			Icon(
				imageVector = Icons.Outlined.Add,
				modifier = Modifier.fillMaxSize(),
				contentDescription = "+"
			)
		}
	)

	Text(
		text = annotatedString,
		textAlign = TextAlign.Center,
		inlineContent = inlineContentMap,
		modifier = modifier
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomFinderHourSelector(state: RoomFinderState) {
	state.currentUnit?.let { unit ->
		ListItem(
			headlineText = {
				Text(
					text = stringResource(
						id = R.string.roomfinder_current_hour,
						state.translateDay(unit.first.day),
						unit.second
					),
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth()
				)
			},
			supportingText = {
				Text(
					text = stringResource(
						id = R.string.roomfinder_current_hour_time,
						unit.third.startTime.toLocalTime()
							.toString(DateTimeFormat.shortTime()),
						unit.third.endTime.toLocalTime()
							.toString(DateTimeFormat.shortTime())
					),
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth()
				)
			},
			leadingContent = {
				IconButton(
					enabled = state.hourIndexCanDecrease,
					onClick = { state.onDecreaseHourIndex() }
				) {
					Icon(
						painter = painterResource(id = R.drawable.roomfinder_previous),
						contentDescription = stringResource(id = R.string.roomfinder_image_previous_hour)
					)
				}
			},
			trailingContent = {
				IconButton(
					enabled = state.hourIndexCanIncrease,
					onClick = { state.onIncreaseHourIndex() }
				) {
					Icon(
						painter = painterResource(id = R.drawable.roomfinder_next),
						contentDescription = stringResource(id = R.string.roomfinder_image_next_hour)
					)
				}
			},
			modifier = Modifier
				.clickable { state.onResetHourIndex() }
		)
	}
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun RoomListItem(
	item: RoomFinderState.RoomStatusData,
	hourIndex: Int,
	onDelete: (() -> Unit)? = null,
	modifier: Modifier = Modifier
) {
	val state = item.getState(hourIndex)

	val isFree = !item.isError && state >= ROOM_STATE_FREE
	val isOccupied = !item.isError && state == ROOM_STATE_OCCUPIED

	// TODO: Show "Free for the rest of the day/week" (if applicable)
	ListItem(
		headlineText = { Text(item.name) },
		supportingText = {
			Text(
				when {
					isOccupied -> stringResource(R.string.roomfinder_item_desc_occupied)
					isFree -> pluralStringResource(R.plurals.roomfinder_item_desc, state, state)
					item.isLoading -> stringResource(R.string.roomfinder_loading_data)
					else -> item.errorMessage?.let {
						stringResource(R.string.roomfinder_error_details, it)
					} ?: stringResource(R.string.roomfinder_error)
				}
			)
		},
		leadingContent = if (item.isLoading) {
			{ CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
		} else {
			{
				Icon(
					painter = painterResource(
						id = when {
							isOccupied -> R.drawable.all_cross
							isFree -> R.drawable.all_check
							else -> R.drawable.all_error
						}
					),
					tint = when {
						isOccupied -> MaterialTheme.colorScheme.error
						isFree -> MaterialTheme.colorScheme.primary
						else -> LocalContentColor.current
					},
					contentDescription = stringResource(id = R.string.roomfinder_image_availability_indicator)
				)
			}
		},
		trailingContent = onDelete?.let {
			{
				IconButton(onClick = onDelete) {
					Icon(
						imageVector = Icons.Outlined.Delete,
						contentDescription = stringResource(id = R.string.roomfinder_delete_item)
					)
				}
			}
		},
		modifier = modifier
	)
}

class RoomFinderState constructor(
	private val user: LegacyUserDatabase.User,
	val timetableDatabaseInterface: TimetableDatabaseInterface,
	private val preferences: DataStorePreferences,
	private val contextActivity: Activity,
	private val scope: CoroutineScope,
	private var hourIndex: MutableState<Int>,
	private var showElementPicker: MutableState<Boolean>,
	private val roomFinderDatabase: RoomFinderDatabase = RoomFinderDatabase.createInstance(contextActivity, user.id)
) {
	companion object {
		const val ROOM_STATE_OCCUPIED = 0
		const val ROOM_STATE_FREE = 1
		const val ROOM_STATE_LOADING = -1

		const val DELETE_ITEM_NONE = -1
	}

	val isRoomListEmpty: Boolean
		get() = roomList.isEmpty()

	val shouldShowElementPicker: Boolean
		get() = showElementPicker.value

	val shouldShowDeleteItem: Boolean
		get() = deleteItem != DELETE_ITEM_NONE

	val currentDeleteItem: RoomStatusData?
		get() = roomList.find { it.periodElement.id == deleteItem }

	val currentUnit: Triple<Day, Int, com.sapuseven.untis.models.untis.masterdata.timegrid.Unit>?
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
					TimetableDatabaseInterface.Type.ROOM.name, it.id, it.id
				),
				timetableDatabaseInterface,
				it.states
			)
		}.toTypedArray()
	)

	private fun calculateMaxHourIndex(user: LegacyUserDatabase.User): Int {
		var maxHourIndex = -1 // maxIndex = -1 + length
		user.timeGrid.days.forEach { day ->
			maxHourIndex += day.units.size
		}
		return maxHourIndex
	}

	@Throws(TimetableLoader.TimetableLoaderException::class)
	private suspend fun loadStates(
		user: LegacyUserDatabase.User,
		roomId: Int,
		proxyHost: String?
	): List<Boolean> {
		val states = mutableListOf<Boolean>()

		val startDate = UntisDate.fromLocalDate(
			LocalDate.now().withDayOfWeek(
				DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH)
					.parseDateTime(user.timeGrid.days.first().day).dayOfWeek
			)
		)
		val endDate = UntisDate.fromLocalDate(
			LocalDate.now().withDayOfWeek(
				DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH)
					.parseDateTime(user.timeGrid.days.last().day).dayOfWeek
			)
		)

		// Dummy Data:
		/*delay(1000 + nextLong(0, 2000))
		for (i in 0..10)
			states.add(nextBoolean())*/

		TimetableLoader(
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
					val unitStartDateTime = unit.startTime.toLocalTime()
					val unitEndDateTime = unit.endTime.toLocalTime()

					var occupied = false

					timetableItems.items.forEach allItems@{ item ->
						if (item.startDateTime.dayOfWeek == dayDateTime.dayOfWeek)
							if (item.startDateTime.millisOfDay <= unitEndDateTime.millisOfDay
								&& item.endDateTime.millisOfDay >= unitStartDateTime.millisOfDay
							) {
								occupied = true
								return@allItems
							}
					}

					loadedStates.add(occupied)
				}
			}

			states.addAll(loadedStates.toList())
		}

		return states.toList()
	}

	data class RoomStatusData(
		val periodElement: PeriodElement,
		val timetableDatabaseInterface: TimetableDatabaseInterface? = null,
		val states: List<Boolean>? = null,
		val name: String = timetableDatabaseInterface?.getShortName(periodElement) ?: "",
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
		user: LegacyUserDatabase.User,
		index: Int
	): Triple<Day, Int, com.sapuseven.untis.models.untis.masterdata.timegrid.Unit>? {
		var indexCounter = index
		user.timeGrid.days.forEach { day ->
			if (indexCounter >= day.units.size)
				indexCounter -= day.units.size
			else
				return Triple(day, indexCounter + 1, day.units[indexCounter])
		}
		return null
	}

	fun translateDay(day: String): String {
		return DateTimeFormat.forPattern("EEEE")
			.print(DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseDateTime(day))
	}

	fun deleteItem(item: RoomStatusData) {
		if (roomFinderDatabase.deleteRoom(item.periodElement.id))
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
							periodElement.id,
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
	}
}

private fun calculateCurrentHourIndex(user: LegacyUserDatabase.User): Int {
	val now = LocalDateTime.now()
	var index = 0

	user.timeGrid.days.forEach { day ->
		val dayDate =
			DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseLocalDate(day.day)
		if (dayDate.dayOfWeek == now.dayOfWeek) {
			day.units.forEach { unit ->
				if (unit.endTime.toLocalTime().millisOfDay > now.millisOfDay)
					return index
				index++
			}
			return index
		} else {
			index += day.units.size
		}
	}

	return 0
}

@Composable
private fun rememberRoomFinderState(
	user: LegacyUserDatabase.User,
	timetableDatabaseInterface: TimetableDatabaseInterface,
	preferences: DataStorePreferences,
	contextActivity: RoomFinderActivity,
	scope: CoroutineScope = rememberCoroutineScope(),
	hourIndex: MutableState<Int> = rememberSaveable { mutableStateOf(calculateCurrentHourIndex(user)) },
	showElementPicker: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
) = remember(user) {
	RoomFinderState(
		user = user,
		timetableDatabaseInterface = timetableDatabaseInterface,
		preferences = preferences,
		contextActivity = contextActivity,
		scope = scope,
		hourIndex = hourIndex,
		showElementPicker = showElementPicker
	)
}
