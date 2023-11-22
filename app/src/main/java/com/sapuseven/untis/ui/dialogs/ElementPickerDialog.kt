package com.sapuseven.untis.ui.dialogs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults.TextFieldDecorationBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.sapuseven.untis.R
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement
import com.sapuseven.untis.ui.common.AbbreviatedText
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.common.NavigationBarInset
import com.sapuseven.untis.ui.common.disabled
import com.sapuseven.untis.ui.functional.insetsPaddingValues

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElementPickerDialogFullscreen(
	title: @Composable () -> Unit,
	timetableDatabaseInterface: TimetableDatabaseInterface,
	initialType: TimetableDatabaseInterface.Type? = null,
	multiSelect: Boolean = false,
	hideTypeSelection: Boolean = false,
	hideTypeSelectionPersonal: Boolean = false,
	onDismiss: (success: Boolean) -> Unit = {},
	onSelect: (selectedItem: PeriodElement?) -> Unit = {},
	onMultiSelect: (selectedItems: List<PeriodElement>) -> Unit = {},
	additionalActions: (@Composable () -> Unit) = {},
	selectedElements: List<PeriodElement>? = null
) {
	var selectedType by remember { mutableStateOf(initialType) }
	var showSearch by remember { mutableStateOf(false) }
	var search by remember { mutableStateOf("") }

	val items = remember(selectedType) {
		mutableStateMapOf<PeriodElement, Boolean>().apply {
			timetableDatabaseInterface.getElements(selectedType)
				.associateWith {
					selectedElements?.contains(it) ?: false
				}
				.also {
					putAll(it)
				}
		}
	}

	BackHandler {
		onDismiss(false)
	}

	AppScaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = {
					if (!showSearch)
						title()
					else {
						val focusRequester = remember { FocusRequester() }

						BasicTextField(
							value = search,
							onValueChange = { search = it },
							singleLine = true,
							decorationBox = { innerTextField ->
								TextFieldDecorationBox(
									value = search,
									innerTextField = innerTextField,
									enabled = true,
									singleLine = true,
									visualTransformation = VisualTransformation.None,
									interactionSource = remember { MutableInteractionSource() },
									placeholder = { Text("Search") },
									contentPadding = PaddingValues(horizontal = 0.dp),
									colors = TextFieldDefaults.textFieldColors(
										containerColor = Color.Transparent,
										focusedIndicatorColor = Color.Transparent,
										unfocusedIndicatorColor = Color.Transparent,
										disabledIndicatorColor = Color.Transparent
									)
								)
							},
							textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
							cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
							modifier = Modifier
								.fillMaxWidth()
								.padding(20.dp)
								.focusRequester(focusRequester)
						)

						LaunchedEffect(Unit) {
							focusRequester.requestFocus()
						}
					}
				},
				navigationIcon = {
					if (showSearch)
						IconButton(onClick = {
							showSearch = false
							search = ""
						}) {
							Icon(
								imageVector = Icons.Outlined.ArrowBack,
								contentDescription = stringResource(id = R.string.all_back)
							)
						}
					else
						IconButton(onClick = { onDismiss(false) }) {
							Icon(
								imageVector = Icons.Outlined.Close,
								contentDescription = "TODO"
							)
						}
				},
				actions = {
					if (!showSearch) {
						IconButton(onClick = { showSearch = true }) {
							Icon(
								imageVector = Icons.Outlined.Search,
								contentDescription = "TODO"
							)
						}
					}

					additionalActions()

					if (multiSelect) {
						IconButton(onClick = {
							onMultiSelect(items.filter { it.value }.keys.toList())
							onDismiss(true)
						}) {
							Icon(
								imageVector = Icons.Outlined.Check,
								contentDescription = "TODO"
							)
						}
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
			ElementPickerElements(
				timetableDatabaseInterface = timetableDatabaseInterface,
				selectedType = selectedType,
				multiSelect = multiSelect,
				onDismiss = onDismiss,
				onSelect = onSelect,
				items = items,
				filter = search,
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f),
				contentPadding = if (hideTypeSelection) insetsPaddingValues() else PaddingValues(0.dp)
			)

			if (!hideTypeSelection)
				ElementPickerTypeSelection(
					selectedType = selectedType,
					hideTypeSelectionPersonal = hideTypeSelectionPersonal,
					onTypeChange = { selectedType = it },
					onDismiss = onDismiss,
					onSelect = onSelect
				)
		}
	}
}

/**
 * A minimal dialog version of the element picker. Missing features
 * currently are: toolbar actions, close button.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ElementPickerDialog(
	title: (@Composable () -> Unit)?,
	timetableDatabaseInterface: TimetableDatabaseInterface,
	initialType: TimetableDatabaseInterface.Type? = null,
	multiSelect: Boolean = false,
	hideTypeSelection: Boolean = false,
	hideTypeSelectionPersonal: Boolean = false,
	onDismiss: (success: Boolean) -> Unit = {},
	onSelect: (selectedItem: PeriodElement?) -> Unit = {},
	onMultiSelect: (selectedItems: List<PeriodElement>) -> Unit = {},
	initialSelection: List<PeriodElement>? = null
) {
	var selectedType by remember { mutableStateOf(initialType) }

	val items = remember(selectedType) {
		mutableStateMapOf<PeriodElement, Boolean>().apply {
			timetableDatabaseInterface.getElements(selectedType)
				.associateWith {
					initialSelection?.contains(it) ?: false
				}
				.also {
					putAll(it)
				}
		}
	}

	Dialog(onDismissRequest = { onDismiss(false) }) {
		Surface(
			modifier = Modifier.padding(vertical = 24.dp),
			shape = AlertDialogDefaults.shape,
			color = AlertDialogDefaults.containerColor,
			tonalElevation = AlertDialogDefaults.TonalElevation
		) {
			Column {
				title?.let {
					ProvideTextStyle(value = MaterialTheme.typography.headlineSmall) {
						Box(
							Modifier
								.padding(top = 24.dp, bottom = 16.dp)
								.padding(horizontal = 24.dp)
						) {
							title()
						}
					}
				}

				if (multiSelect) {
					val interactionSource = remember { MutableInteractionSource() }
					val selectAll: ((Boolean) -> Unit) = { selectState ->
						items.forEach { item, _ -> items[item] = selectState }
					}

					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier
							.padding(horizontal = 24.dp)
					) {
						Checkbox(
							checked = !items.values.contains(false),
							onCheckedChange = selectAll,
							interactionSource = interactionSource
						)

						Text(
							text = stringResource(id = R.string.elementpicker_select_all),
							style = MaterialTheme.typography.bodyLarge,
							modifier = Modifier
								.clickable(
									interactionSource = interactionSource,
									indication = null,
									role = Role.Checkbox
								) {
									selectAll(items.values.contains(false))
								}
								.weight(1f)
						)
					}

					Divider()
				}

				ElementPickerElements(
					timetableDatabaseInterface = timetableDatabaseInterface,
					selectedType = selectedType,
					multiSelect = multiSelect,
					onDismiss = onDismiss,
					onSelect = onSelect,
					items = items,
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
						.padding(horizontal = 24.dp)
				)
				//Spacer(modifier = Modifier.weight(0.5f))

				if (!hideTypeSelection)
					ElementPickerTypeSelection(
						selectedType = selectedType,
						hideTypeSelectionPersonal = hideTypeSelectionPersonal,
						onTypeChange = { selectedType = it },
						onDismiss = onDismiss,
						onSelect = onSelect
					)

				if (multiSelect) {
					FlowRow(
						mainAxisAlignment = MainAxisAlignment.End,
						mainAxisSpacing = 8.dp,
						modifier = Modifier
							.fillMaxWidth()
							.padding(top = 16.dp, bottom = 24.dp)
							.padding(horizontal = 24.dp),
					) {
						TextButton(onClick = {
							onDismiss(false)
						}) {
							Text(text = stringResource(id = R.string.all_cancel))
						}
						TextButton(onClick = {
							onMultiSelect(items.filter { it.value }.keys.toList())
							onDismiss(true)
						}) {
							Text(text = stringResource(id = R.string.all_ok))
						}
					}
				}
			}
		}
	}
}


@Composable
fun ElementPickerElements(
	timetableDatabaseInterface: TimetableDatabaseInterface,
	selectedType: TimetableDatabaseInterface.Type?,
	multiSelect: Boolean = false,
	modifier: Modifier,
	onDismiss: (success: Boolean) -> Unit = {},
	onSelect: (selectedItem: PeriodElement?) -> Unit = {},
	items: MutableMap<PeriodElement, Boolean>,
	filter: String = "",
	contentPadding: PaddingValues = PaddingValues(0.dp)
) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
	) {
		selectedType?.let { type ->
			LazyVerticalGrid(
				columns = GridCells.Adaptive(if (multiSelect) 128.dp else 96.dp),
				modifier = Modifier.fillMaxHeight(),
				contentPadding = contentPadding
			) {
				items(
					items = items.keys
						.map {
							object {
								val element = it
								val name = timetableDatabaseInterface.getShortName(it)
								val enabled = if (type != TimetableDatabaseInterface.Type.SUBJECT) {
									timetableDatabaseInterface.isAllowed(it)
								} else {
									true
								}
							}
						}
						.filter { it.name.contains(filter, true) }
						.sortedWith(compareBy({ !it.enabled }, { it.name })),
					key = { it.hashCode() }
				) { item ->
					val interactionSource = remember { MutableInteractionSource() }

					Row(
						verticalAlignment = Alignment.CenterVertically
					) {
						if (multiSelect)
							Checkbox(
								checked = items[item.element] ?: false,
								onCheckedChange = {
									onSelect(item.element)
									items[item.element] = it
								},
								interactionSource = interactionSource,
								enabled = item.enabled
							)

						Text(
							text = item.name,
							style = MaterialTheme.typography.bodyLarge,
							modifier = Modifier
								.clickable(
									interactionSource = interactionSource,
									indication = if (!multiSelect) LocalIndication.current else null,
									role = Role.Checkbox,
									enabled = item.enabled
								) {
									onSelect(item.element)
									if (multiSelect)
										items[item.element] = items[item.element] == false
									else
										onDismiss(true)
								}
								.weight(1f)
								.padding(
									vertical = 16.dp,
									horizontal = if (!multiSelect) 16.dp else 0.dp
								)
								.disabled(!item.enabled)
						)
					}
				}
			}
		}

		if (selectedType == null) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Icon(
					imageVector = Icons.Outlined.Info,
					contentDescription = null,
					modifier = Modifier
						.size(96.dp)
						.padding(bottom = 24.dp)
				)
				Text(stringResource(R.string.elementpicker_timetable_select))
			}
		}
	}
}

@Composable
fun ElementPickerTypeSelection(
	selectedType: TimetableDatabaseInterface.Type?,
	hideTypeSelectionPersonal: Boolean = false,
	onTypeChange: (TimetableDatabaseInterface.Type?) -> Unit,
	onDismiss: (success: Boolean) -> Unit = {},
	onSelect: (selectedItem: PeriodElement?) -> Unit = {}
) {
	NavigationBarInset {
		if (!hideTypeSelectionPersonal)
			NavigationBarItem(
				icon = {
					Icon(
						painterResource(id = R.drawable.all_prefs_personal),
						contentDescription = null
					)
				},
				label = {
					AbbreviatedText(
						text = stringResource(id = R.string.all_personal),
						abbreviatedText = stringResource(id = R.string.all_personal_abbr)
					)
				},
				selected = false,
				onClick = {
					onSelect(null)
					onDismiss(true)
				}
			)

		NavigationBarItem(
			icon = {
				Icon(
					painterResource(id = R.drawable.all_classes),
					contentDescription = null
				)
			},
			label = { Text(stringResource(id = R.string.all_classes)) },
			selected = selectedType == TimetableDatabaseInterface.Type.CLASS,
			onClick = { onTypeChange(TimetableDatabaseInterface.Type.CLASS) }
		)
		NavigationBarItem(
			icon = {
				Icon(
					painterResource(id = R.drawable.all_teachers),
					contentDescription = null
				)
			},
			label = { Text(stringResource(id = R.string.all_teachers)) },
			selected = selectedType == TimetableDatabaseInterface.Type.TEACHER,
			onClick = { onTypeChange(TimetableDatabaseInterface.Type.TEACHER) }
		)
		NavigationBarItem(
			icon = {
				Icon(
					painterResource(id = R.drawable.all_rooms),
					contentDescription = null
				)
			},
			label = { Text(stringResource(id = R.string.all_rooms)) },
			selected = selectedType == TimetableDatabaseInterface.Type.ROOM,
			onClick = { onTypeChange(TimetableDatabaseInterface.Type.ROOM) }
		)
	}
}
