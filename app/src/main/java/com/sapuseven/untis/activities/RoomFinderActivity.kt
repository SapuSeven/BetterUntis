package com.sapuseven.untis.activities

import android.os.Bundle
import androidx.activity.compose.setContent

class RoomFinderActivity : BaseComposeActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			AppTheme(navBarInset = false) {
				withUser { user ->
					/*val state = rememberRoomFinderState(
						user,
						timetableDatabaseInterface,
						dataStorePreferences,
						this
					)
					RoomFinder(state)*/
				}
			}
		}
	}
}

/*@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
		/*ElementPickerDialogFullscreen(
			title = { Text(stringResource(id = R.string.all_add)) }, // TODO: Proper string resource
			multiSelect = true,
			hideTypeSelection = true,
			initialType = TimetableDatabaseInterface.Type.ROOM,
			timetableDatabaseInterface = state.timetableDatabaseInterface,
			onDismiss = { state.onElementPickerDismiss() },
			onMultiSelect = { state.onElementPickerSelect(it) }
		)*/
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

class RoomFinderState constructor(
	private val user: User,
	val timetableDatabaseInterface: TimetableDatabaseInterface,
	private val preferences: DataStorePreferences,
	private val contextActivity: Activity,
	private val scope: CoroutineScope,
	private var hourIndex: MutableState<Int>,
	private var showElementPicker: MutableState<Boolean>,
	private val roomFinderDatabase: RoomFinderDatabase = RoomFinderDatabase.createInstance(
		contextActivity,
		user.id
	)
) {
	companion object {
		const val ROOM_STATE_OCCUPIED = 0
		const val ROOM_STATE_FREE = 1
		const val ROOM_STATE_LOADING = -1

		const val DELETE_ITEM_NONE = -1L
	}

}

private fun calculateCurrentHourIndex(user: User): Int {
	// TODO
	/*val now = LocalDateTime.now()
	var index = 0

	user.timeGrid.days.forEach { day ->
		val dayDate =
			DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseLocalDate(day.day)
		if (dayDate.dayOfWeek == now.dayOfWeek) {
			day.units.forEach { unit ->
				if (LocalTime(unit.endTime).millisOfDay > now.millisOfDay)
					return index
				index++
			}
			return index
		} else {
			index += day.units.size
		}
	}*/

	return 0
}

@Composable
private fun rememberRoomFinderState(
	user: User,
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
}*/
