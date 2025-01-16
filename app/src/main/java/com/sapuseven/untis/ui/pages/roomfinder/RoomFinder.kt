package com.sapuseven.untis.ui.pages.roomfinder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationEnter
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationExit
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.functional.bottomInsets

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RoomFinder(
	viewModel: RoomFinderViewModel = hiltViewModel()
) {
	AppScaffold(
		modifier = Modifier.bottomInsets(),
		topBar = {
			CenterAlignedTopAppBar(
				title = {
					Text(stringResource(id = R.string.activity_title_free_rooms))
				},
				navigationIcon = {
					IconButton(onClick = { viewModel.goBack() }) {
						Icon(
							imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
							contentDescription = stringResource(id = R.string.all_back)
						)
					}
				},
				actions = {
					IconButton(onClick = { viewModel.onAddButtonClick() }) {
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
					/*items(
						viewModel.sortedRoomList,
						key = { it.periodElement.id }
					) {
						RoomListItem(
							item = it,
							hourIndex = viewModel.currentHourIndex,
							onDelete = { viewModel.onRoomListItemDeleteClick(it) },
							modifier = Modifier
								.animateItem()
								.clickable { viewModel.onRoomListItemClick(it) }
						)
					}*/
				}

				/*if (viewModel.isRoomListEmpty)
					RoomFinderListEmpty(
						modifier = Modifier
							.align(Alignment.CenterHorizontally)
							.weight(1f)
					)
				else
					RoomFinderHourSelector(viewModel)*/
			}
		}
	}

	val showElementPicker by viewModel.showElementPicker.collectAsState()
	AnimatedVisibility(
		visible = showElementPicker,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		/*ElementPickerDialogFullscreen(
			title = { Text(stringResource(id = R.string.all_add)) }, // TODO: Proper string resource
			multiSelect = true,
			hideTypeSelection = true,
			initialType = TimetableDatabaseInterface.Type.ROOM,
			timetableDatabaseInterface = viewModel.timetableDatabaseInterface,
			onDismiss = { viewModel.onElementPickerDismiss() },
			onMultiSelect = { viewModel.onElementPickerSelect(it) }
		)*/
	}

	/*if (viewModel.shouldShowDeleteItem) {
		viewModel.currentDeleteItem?.let { item ->
			AlertDialog(
				onDismissRequest = {
					viewModel.onDeleteItemDialogDismiss()
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
							viewModel.onDeleteItemDialogDismiss()
							viewModel.deleteItem(item)
						}) {
						Text(stringResource(id = R.string.all_yes))
					}
				},
				dismissButton = {
					TextButton(
						onClick = {
							viewModel.onDeleteItemDialogDismiss()
						}) {
						Text(stringResource(id = R.string.all_no))
					}
				}
			)
		}
	}*/
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

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomFinderHourSelector(viewModel: RoomFinderState) {
	viewModel.currentUnit?.let { unit ->
		ListItem(
			headlineContent = {
				Text(
					text = stringResource(
						id = R.string.roomfinder_current_hour,
						viewModel.translateDay(unit.first.day),
						unit.second
					),
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth()
				)
			},
			supportingContent = {
				Text(
					text = stringResource(
						id = R.string.roomfinder_current_hour_time,
						LocalTime(unit.third.startTime)
							.toString(DateTimeFormat.shortTime()),
						LocalTime(unit.third.endTime)
							.toString(DateTimeFormat.shortTime())
					),
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth()
				)
			},
			leadingContent = {
				IconButton(
					enabled = viewModel.hourIndexCanDecrease,
					onClick = { viewModel.onDecreaseHourIndex() }
				) {
					Icon(
						painter = painterResource(id = R.drawable.roomfinder_previous),
						contentDescription = stringResource(id = R.string.roomfinder_image_previous_hour)
					)
				}
			},
			trailingContent = {
				IconButton(
					enabled = viewModel.hourIndexCanIncrease,
					onClick = { viewModel.onIncreaseHourIndex() }
				) {
					Icon(
						painter = painterResource(id = R.drawable.roomfinder_next),
						contentDescription = stringResource(id = R.string.roomfinder_image_next_hour)
					)
				}
			},
			modifier = Modifier
				.clickable { viewModel.onResetHourIndex() }
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
		headlineContent = { Text(item.name) },
		supportingContent = {
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
}*/
