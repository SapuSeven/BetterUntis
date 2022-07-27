package com.sapuseven.untis.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults.TextFieldDecorationBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.untis.timetable.PeriodElement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElementPickerDialogFullscreen(
	title: @Composable () -> Unit,
	timetableDatabaseInterface: TimetableDatabaseInterface,
	initialType: TimetableDatabaseInterface.Type? = null,
	multiSelect: Boolean = false,
	hideTypeSelection: Boolean = false,
	onDismiss: () -> Unit,
	onSelect: (PeriodElement?) -> Unit
) {
	var showSearch by remember { mutableStateOf(false) }
	var search by remember { mutableStateOf("") }

	/*userDatabase = UserDatabase.createInstance(this)
	userDatabase.getUser(intent.getLongExtra(EXTRA_LONG_PROFILE_ID, -1))?.let { user = it }
	val timetableDatabase = rememberTimetableDatabase(userDatabase, user.id!!)*/

	Scaffold(
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
							decorationBox = { innerTextField ->
								TextFieldDecorationBox(
									value = search,
									innerTextField = innerTextField,
									enabled = true,
									singleLine = true,
									visualTransformation = VisualTransformation.None,
									interactionSource = remember { MutableInteractionSource() },
									placeholder = { Text("Search") },
									contentPadding = PaddingValues(horizontal = 0.dp)
								)
							},
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
						IconButton(onClick = { showSearch = false }) {
							Icon(
								imageVector = Icons.Filled.ArrowBack,
								contentDescription = "TODO"
							)
						}
					else
						IconButton(onClick = { onDismiss() }) {
							Icon(
								imageVector = Icons.Filled.Close,
								contentDescription = "TODO"
							)
						}
				},
				actions = {
					if (!showSearch) {
						IconButton(onClick = { showSearch = true }) {
							Icon(
								imageVector = Icons.Filled.Search,
								contentDescription = "TODO"
							)
						}
					}
				}
			)
		}
	) { innerPadding ->
		var selectedType by remember { mutableStateOf(initialType) }
		val items = timetableDatabaseInterface.getElements(selectedType)

		Column(
			modifier = Modifier
				.padding(innerPadding)
				.fillMaxSize()
		) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
			) {
				LazyVerticalGrid(
					columns = GridCells.Adaptive(96.dp),
					modifier = Modifier.fillMaxHeight()
				) {
					items(items) {
						if (multiSelect)
							LabeledCheckbox(checked = false, onCheckedChange = {}) {
								Text(timetableDatabaseInterface.getShortName(it.id, selectedType))
							}
						else
							Text(
								text = timetableDatabaseInterface.getShortName(it.id, selectedType),
								modifier = Modifier.clickable { onSelect(it) }
							)
					}
				}
			}

			if (!hideTypeSelection)
				NavigationBar {
					NavigationBarItem(
						icon = {
							Icon(
								painterResource(id = R.drawable.all_prefs_personal),
								contentDescription = null
							)
						},
						label = { Text(stringResource(id = R.string.all_personal)) },
						selected = false,
						onClick = { onSelect(null) }
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
						onClick = { selectedType = TimetableDatabaseInterface.Type.CLASS }
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
						onClick = { selectedType = TimetableDatabaseInterface.Type.TEACHER }
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
						onClick = { selectedType = TimetableDatabaseInterface.Type.ROOM }
					)
				}
		}
	}
}
