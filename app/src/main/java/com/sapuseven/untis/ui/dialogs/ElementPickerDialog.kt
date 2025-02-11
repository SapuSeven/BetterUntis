package com.sapuseven.untis.ui.dialogs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.asFlow
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.components.ElementPicker
import com.sapuseven.untis.ui.common.AbbreviatedText
import com.sapuseven.untis.ui.common.AppScaffold
import com.sapuseven.untis.ui.common.NavigationBarInset
import com.sapuseven.untis.ui.common.disabled
import com.sapuseven.untis.ui.functional.insetsPaddingValues

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElementPickerDialogFullscreenNew(
	elementPicker: ElementPicker,
	title: @Composable () -> Unit,
	initialType: ElementType? = null,
	multiSelect: Boolean = false,
	hideTypeSelection: Boolean = false,
	hideTypeSelectionPersonal: Boolean = false,
	onDismiss: (success: Boolean) -> Unit = {},
	onSelect: (selectedItem: PeriodElement?) -> Unit = {},
	onMultiSelect: (selectedItems: List<PeriodElement>) -> Unit = {},
	additionalActions: (@Composable () -> Unit) = {},
) {
	var selectedType by remember { mutableStateOf(initialType) }
	var showSearch by remember { mutableStateOf(false) }
	var search by remember { mutableStateOf("") }

	val items = remember { mutableStateMapOf<PeriodElement, Boolean>() }
	LaunchedEffect(selectedType) {
		items.clear()
		elementPicker.getAllPeriodElements()[selectedType]?.asFlow()?.collect { periodElements ->
			periodElements.associateWith { false }.let { items.putAll(it) }
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
								TextFieldDefaults.DecorationBox(
									value = search,
									innerTextField = innerTextField,
									enabled = true,
									singleLine = true,
									visualTransformation = VisualTransformation.None,
									interactionSource = remember { MutableInteractionSource() },
									placeholder = { Text("Search") },
									contentPadding = PaddingValues(horizontal = 0.dp),
									colors = TextFieldDefaults.colors(
										focusedContainerColor = Color.Transparent,
										unfocusedContainerColor = Color.Transparent,
										errorContainerColor = Color.Transparent,
										disabledContainerColor = Color.Transparent,
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
			//Text(text = "${elements.get(ElementType.ROOM)?.size ?: "?"} rooms")
			ElementPickerElementsNew(
				elementPicker = elementPicker,
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

@Composable
fun ElementPickerDialogNew(
	elementPicker: ElementPicker,
	title: (@Composable () -> Unit)?,
	initialType: ElementType? = null,
	hideTypeSelection: Boolean = false,
	hideTypeSelectionPersonal: Boolean = false,
	onDismiss: (success: Boolean) -> Unit = {},
	onSelect: (selectedItem: PeriodElement?) -> Unit = {}
) {
	var selectedType by remember { mutableStateOf(initialType) }

	val items = remember { mutableStateMapOf<PeriodElement, Boolean>() }
	LaunchedEffect(selectedType) {
		items.clear()
		elementPicker.getAllPeriodElements()[selectedType]?.asFlow()?.collect { periodElements ->
			periodElements.associateWith { false }.let { items.putAll(it) }
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

				ElementPickerElementsNew(
					elementPicker = elementPicker,
					selectedType = selectedType,
					onDismiss = onDismiss,
					onSelect = onSelect,
					items = items,
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
						.padding(horizontal = 24.dp)
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
}

@Composable
fun ElementPickerElementsNew(
	elementPicker: ElementPicker,
	selectedType: ElementType?,
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
		selectedType?.let {
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
								val name = elementPicker.getShortName(it)
								val enabled = elementPicker.isAllowed(it)
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
								onCheckedChange = { items[item.element] = it },
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
	selectedType: ElementType?,
	hideTypeSelectionPersonal: Boolean = false,
	onTypeChange: (ElementType?) -> Unit,
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
			selected = selectedType == ElementType.CLASS,
			onClick = { onTypeChange(ElementType.CLASS) }
		)
		NavigationBarItem(
			icon = {
				Icon(
					painterResource(id = R.drawable.all_teachers),
					contentDescription = null
				)
			},
			label = { Text(stringResource(id = R.string.all_teachers)) },
			selected = selectedType == ElementType.TEACHER,
			onClick = { onTypeChange(ElementType.TEACHER) }
		)
		NavigationBarItem(
			icon = {
				Icon(
					painterResource(id = R.drawable.all_rooms),
					contentDescription = null
				)
			},
			label = { Text(stringResource(id = R.string.all_rooms)) },
			selected = selectedType == ElementType.ROOM,
			onClick = { onTypeChange(ElementType.ROOM) }
		)
	}
}
