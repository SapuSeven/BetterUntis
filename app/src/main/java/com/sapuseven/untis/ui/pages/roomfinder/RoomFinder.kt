package com.sapuseven.untis.ui.pages.roomfinder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.models.RoomFinderHour
import com.sapuseven.untis.models.RoomFinderItem
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationEnter
import com.sapuseven.untis.ui.animations.fullscreenDialogAnimationExit
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.common.SmallCircularProgressIndicator
import com.sapuseven.untis.ui.dialogs.ElementPickerDialogFullscreen
import com.sapuseven.untis.ui.functional.bottomInsets
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomFinder(
	viewModel: RoomFinderViewModel = hiltViewModel()
) {
	var showElementPicker by rememberSaveable { mutableStateOf(false) }

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
					IconButton(onClick = { showElementPicker = true }) {
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
				val roomList by viewModel.roomList.collectAsStateWithLifecycle()

				val hours by viewModel.hourList.collectAsStateWithLifecycle()
				val selectedIndex by viewModel.selectedHourIndex.collectAsStateWithLifecycle()

				LazyColumn(
					Modifier
						.fillMaxWidth()
						.weight(1f)
				) {
					items(
						roomList,
						key = { it.entity.id }
					) {
						RoomListItem(
							item = it,
							hours = hours,
							hourIndex = selectedIndex,
							onDelete = { viewModel.deleteRoom(it) },
							modifier = Modifier
								.animateItem()
								.clickable { viewModel.onRoomClick(it) }
						)
					}
				}

				if (roomList.isEmpty())
					RoomFinderListEmpty(
						modifier = Modifier
							.align(Alignment.CenterHorizontally)
							.weight(1f)
					)
				else
					RoomFinderHourSelector(hours, selectedIndex) {
						viewModel.selectHour(it)
					}
			}
		}
	}

	AnimatedVisibility(
		visible = showElementPicker,
		enter = fullscreenDialogAnimationEnter(),
		exit = fullscreenDialogAnimationExit()
	) {
		ElementPickerDialogFullscreen(
			title = { Text(stringResource(id = R.string.all_add)) }, // TODO: Proper string resource
			multiSelect = true,
			hideTypeSelection = true,
			initialType = ElementType.ROOM,
			onDismiss = { showElementPicker = false },
			onMultiSelect = { viewModel.addRooms(it) },
			masterDataRepository = viewModel.masterDataRepository
		)
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
fun RoomFinderHourSelector(
	hours: List<RoomFinderHour>,
	selectedIndex: Int,
	onSelectionChange: (Int?) -> Unit
) {
	hours[selectedIndex].let { hour ->
		ListItem(
			headlineContent = {
				Text(
					text = stringResource(
						id = R.string.roomfinder_current_hour,
						hour.timeGridDay.day.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()),
						hour.timeGridUnit.label
					),
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth()
				)
			},
			supportingContent = {
				Text(
					text = stringResource(
						id = R.string.roomfinder_current_hour_time,
						hour.timeGridUnit.startTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
						hour.timeGridUnit.endTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
					),
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth()
				)
			},
			leadingContent = {
				IconButton(
					enabled = selectedIndex > 0,
					onClick = { onSelectionChange(selectedIndex - 1) }
				) {
					Icon(
						painter = painterResource(id = R.drawable.roomfinder_previous),
						contentDescription = stringResource(id = R.string.roomfinder_image_previous_hour)
					)
				}
			},
			trailingContent = {
				IconButton(
					enabled = selectedIndex < hours.lastIndex,
					onClick = { onSelectionChange(selectedIndex + 1) }
				) {
					Icon(
						painter = painterResource(id = R.drawable.roomfinder_next),
						contentDescription = stringResource(id = R.string.roomfinder_image_next_hour)
					)
				}
			},
			modifier = Modifier
				.clickable { onSelectionChange(null) }
		)
	}
}

@Composable
fun RoomListItem(
	item: RoomFinderItem,
	hours: List<RoomFinderHour>,
	hourIndex: Int,
	onDelete: (() -> Unit)? = null,
	modifier: Modifier = Modifier,
) {
	// Potential improvement: handle loading errors

	val state = item.freeHoursAt(hourIndex)

	val currentDay = hours[hourIndex].timeGridDay
	val hoursLeftInDay = hours.drop(hourIndex).count { it.timeGridDay == currentDay }
	val hoursLeftInWeek = hours.drop(hourIndex).count()

	val isLoading = item.states.isEmpty()
	val isFree = !isLoading && state > 0
	val isOccupied = !isLoading && state == 0

	ListItem(
		headlineContent = { Text(item.entity.longName) },
		supportingContent = {
			Text(
				when {
					isLoading -> stringResource(R.string.roomfinder_loading_data)
					isOccupied -> stringResource(R.string.roomfinder_item_desc_occupied)
					state >= hoursLeftInWeek -> stringResource(R.string.roomfinder_item_desc_free_week)
					state >= hoursLeftInDay -> stringResource(R.string.roomfinder_item_desc_free_day)
					isFree -> pluralStringResource(R.plurals.roomfinder_item_desc, state, state)
					else -> stringResource(R.string.roomfinder_error)
				}
			)
		},
		leadingContent = {
			if (isLoading) {
				SmallCircularProgressIndicator()
			} else {
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
