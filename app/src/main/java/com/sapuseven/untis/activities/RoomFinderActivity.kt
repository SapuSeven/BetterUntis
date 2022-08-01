package com.sapuseven.untis.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.sapuseven.untis.data.databases.RoomfinderDatabase
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.helpers.timetable.TimetableLoader
import com.sapuseven.untis.models.RoomFinderItem
import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.masterdata.timegrid.Day
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.ui.common.ElementPickerDialogFullscreen
import com.sapuseven.untis.ui.theme.AppTheme
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.lang.ref.WeakReference
import java.util.*

class RoomFinderActivity : BaseComposeActivity() {
	private lateinit var userDatabase: UserDatabase
	private lateinit var user: UserDatabase.User
	private lateinit var timetableDatabaseInterface: TimetableDatabaseInterface
	private lateinit var roomFinderDatabase: RoomfinderDatabase

	companion object {
		const val EXTRA_INT_ROOM_ID = "com.sapuseven.untis.activities.roomid"

		const val ROOM_STATE_OCCUPIED = 0
		const val ROOM_STATE_FREE = 1
		const val ROOM_STATE_LOADING = -1

		const val DELETE_ITEM_NONE = -1
	}

	@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val profileId = intent.getLongExtra(EXTRA_LONG_PROFILE_ID, -1)
		userDatabase = UserDatabase.createInstance(this)
		userDatabase.getUser(profileId)?.let { user = it }
		// TODO: Move this part to BaseComposeActivity and check if user is set

		roomFinderDatabase = RoomfinderDatabase.createInstance(this, profileId)

		timetableDatabaseInterface = TimetableDatabaseInterface(userDatabase, user.id!!)

		val maxHourIndex = calculateMaxHourIndex(user)

		setContent {
			AppTheme {
				val scope = rememberCoroutineScope()

				var showElementPicker by rememberSaveable { mutableStateOf(false) }
				var deleteItem by rememberSaveable { mutableStateOf(DELETE_ITEM_NONE) }
				var hourIndex by rememberSaveable { mutableStateOf(calculateCurrentHourIndex(user)) }

				val hourIndexCanDecrease = hourIndex > 0
				val hourIndexCanIncrease = hourIndex < maxHourIndex
				val unit = getUnitFromIndex(user, hourIndex)

				val roomList = remember {
					mutableStateListOf(
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
				}

				if (showElementPicker)
					ElementPickerDialogFullscreen(
						title = { Text(stringResource(id = R.string.all_add)) }, // TODO: Proper string resource
						multiSelect = true,
						hideTypeSelection = true,
						initialType = TimetableDatabaseInterface.Type.ROOM,
						timetableDatabaseInterface = timetableDatabaseInterface,
						onDismiss = { showElementPicker = false },
						onMultiSelect = { periodElements ->
							showElementPicker = false

							periodElements
								.filter { roomList.find { existing -> existing.periodElement.id == it.id } == null }
								.forEach { periodElement ->
									scope.launch {
										val item = RoomStatusData(
											periodElement,
											timetableDatabaseInterface
										)
										roomList.add(item)
										val states = try {
											loadStates(
												periodElement.id,
												preferences.get<String>(
													"preference_connectivity_proxy_host",
													null
												)
											)
										} catch (e: TimetableLoader.TimetableLoaderException) {
											// TODO: Show error message
											emptyList()
										}

										roomFinderDatabase.addRoom(
											RoomFinderItem(
												periodElement.id,
												item.name,
												states
											)
										)

										roomList.remove(item)
										roomList.add(
											RoomStatusData(
												periodElement,
												timetableDatabaseInterface,
												states
											)
										)
									}
								}
						}
					)
				else
					Scaffold(
						topBar = {
							CenterAlignedTopAppBar(
								title = {
									Text(stringResource(id = R.string.activity_title_free_rooms))
								},
								navigationIcon = {
									IconButton(onClick = { finish() }) {
										Icon(
											imageVector = Icons.Filled.ArrowBack,
											contentDescription = "TODO"
										)
									}
								},
								actions = {
									IconButton(onClick = { showElementPicker = true }) {
										Icon(
											imageVector = Icons.Filled.Add,
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
										roomList.sortedWith(
											compareByDescending<RoomStatusData> {
												it.getState(
													hourIndex
												)
											}
												.thenBy { it.name }
										),
										key = { it.periodElement.id }
									) {
										RoomListItem(
											item = it,
											hourIndex = hourIndex,
											onDelete = { deleteItem = it.periodElement.id },
											modifier = Modifier
												.animateItemPlacement()
												.clickable {
													setResult(
														Activity.RESULT_OK, Intent().putExtra(
															EXTRA_INT_ROOM_ID,
															it.periodElement.id
														)
													)
													finish()
												}
										)
									}
								}

								if (roomList.isEmpty()) {
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
												imageVector = Icons.Default.Add, // TODO: Replace all Filled with Default
												modifier = Modifier.fillMaxSize(),
												contentDescription = "+"
											)
										}
									)

									Text(
										text = annotatedString,
										textAlign = TextAlign.Center,
										inlineContent = inlineContentMap,
										modifier = Modifier
											.align(Alignment.CenterHorizontally)
											.weight(1f)
									)
								} else {
									unit?.let { unit ->
										ListItem(
											headlineText = {
												Text(
													text = stringResource(
														id = R.string.roomfinder_current_hour,
														translateDay(unit.first.day),
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
													enabled = hourIndexCanDecrease,
													onClick = { hourIndex-- }
												) {
													Icon(
														painter = painterResource(id = R.drawable.roomfinder_previous),
														contentDescription = stringResource(id = R.string.roomfinder_image_previous_hour)
													)
												}
											},
											trailingContent = {
												IconButton(
													enabled = hourIndexCanIncrease,
													onClick = { hourIndex++ }
												) {
													Icon(
														painter = painterResource(id = R.drawable.roomfinder_next),
														contentDescription = stringResource(id = R.string.roomfinder_image_next_hour)
													)
												}
											},
											modifier = Modifier
												.clickable {
													hourIndex = calculateCurrentHourIndex(user)
												}
										)
									}
								}
							}
						}
					}

				if (deleteItem != DELETE_ITEM_NONE) {
					roomList.find { it.periodElement.id == deleteItem }?.let { item ->
						AlertDialog(
							onDismissRequest = {
								deleteItem = DELETE_ITEM_NONE
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
								Button(
									onClick = {
										deleteItem = DELETE_ITEM_NONE

										if (roomFinderDatabase.deleteRoom(item.periodElement.id))
											roomList.remove(item)
									}) {
									Text(stringResource(id = R.string.all_yes))
								}
							},
							dismissButton = {
								Button(
									onClick = {
										deleteItem = DELETE_ITEM_NONE
									}) {
									Text(stringResource(id = R.string.all_no))
								}
							}
						)
					}
				}
			}
		}
	}

	private fun calculateMaxHourIndex(user: UserDatabase.User): Int {
		var maxHourIndex = -1 // maxIndex = -1 + length
		user.timeGrid.days.forEach { day ->
			maxHourIndex += day.units.size
		}
		return maxHourIndex
	}

	@Throws(TimetableLoader.TimetableLoaderException::class)
	private suspend fun loadStates(roomId: Int, proxyHost: String?): List<Boolean> {
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
		/*delay(1000 + Random.nextLong(0, 2000))
		for (i in 0..10)
			states.add(Random.nextBoolean())*/

		val result = TimetableLoader(
			context = WeakReference(this@RoomFinderActivity),
			user = user,
			timetableDatabaseInterface = timetableDatabaseInterface
		).loadAsync(
			TimetableLoader.TimetableLoaderTarget(
				startDate,
				endDate,
				roomId,
				TimetableDatabaseInterface.Type.ROOM.name
			), TimetableLoader.FLAG_LOAD_SERVER, proxyHost
		)

		val loadedStates = mutableListOf<Boolean>()
		user.timeGrid.days.forEach { day ->
			val dayDateTime =
				DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH)
					.parseDateTime(day.day)

			day.units.forEach { unit ->
				val unitStartDateTime = unit.startTime.toLocalTime()
				val unitEndDateTime = unit.endTime.toLocalTime()

				var occupied = false
				result.items.forEach allItems@{ item ->
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
		return states.toList()
	}


	data class RoomStatusData(
		val periodElement: PeriodElement,
		val timetableDatabaseInterface: TimetableDatabaseInterface? = null,
		val states: List<Boolean>? = null,
		val name: String = timetableDatabaseInterface?.getShortName(periodElement) ?: "",
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

	@Composable
	@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
	fun RoomListItem(
		item: RoomStatusData,
		hourIndex: Int,
		onDelete: (() -> Unit)? = null,
		modifier: Modifier = Modifier
	) {
		val state = item.getState(hourIndex)

		val isFree = !item.isError && state >= ROOM_STATE_FREE
		val isOccupied = !item.isError && state == ROOM_STATE_OCCUPIED

		ListItem(
			headlineText = { Text(item.name) },
			supportingText = {
				Text(
					when {
						isOccupied -> stringResource(R.string.roomfinder_item_desc_occupied)
						isFree -> pluralStringResource(R.plurals.roomfinder_item_desc, state, state)
						item.isLoading -> stringResource(R.string.roomfinder_loading_data)
						else -> stringResource(R.string.roomfinder_error)
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
							imageVector = Icons.Filled.Delete,
							contentDescription = stringResource(id = R.string.roomfinder_delete_item)
						)
					}
				}
			},
			modifier = modifier
		)
	}

	private fun calculateCurrentHourIndex(user: UserDatabase.User): Int {
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

	private fun translateDay(day: String): String {
		return DateTimeFormat.forPattern("EEEE")
			.print(DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseDateTime(day))
	}

	/**
	 * @return A triple of the day, the unit index of day (1-indexed) and the unit corresponding to the provided hour index.
	 */
	private fun getUnitFromIndex(
		user: UserDatabase.User,
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
}
