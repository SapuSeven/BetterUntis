package com.sapuseven.untis.preferences.preference

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.sapuseven.untis.R
import com.sapuseven.untis.preferences.UntisPreferenceDataStore
import com.sapuseven.untis.ui.colorpicker.ColorPicker
import com.sapuseven.untis.ui.common.disabled
import kotlinx.coroutines.launch

val materialColors = arrayOf(
	Color(0xFFF44336), // RED 500
	Color(0xFFE91E63), // PINK 500
	Color(0xFFFF2C93), // LIGHT PINK 500
	Color(0xFF9C27B0), // PURPLE 500
	Color(0xFF673AB7), // DEEP PURPLE 500
	Color(0xFF3F51B5), // INDIGO 500
	Color(0xFF2196F3), // BLUE 500
	Color(0xFF03A9F4), // LIGHT BLUE 500
	Color(0xFF00BCD4), // CYAN 500
	Color(0xFF009688), // TEAL 500
	Color(0xFF4CAF50), // GREEN 500
	Color(0xFF8BC34A), // LIGHT GREEN 500
	Color(0xFFCDDC39), // LIME 500
	Color(0xFFFFEB3B), // YELLOW 500
	Color(0xFFFFC107), // AMBER 500
	Color(0xFFFF9800), // ORANGE 500
	Color(0xFF795548), // BROWN 500
	Color(0xFF607D8B), // BLUE GREY 500
	Color(0xFF9E9E9E), // GREY 500
	//Color.Black
)

@Composable
fun ColorPreference(
	title: (@Composable () -> Unit),
	icon: (@Composable () -> Unit)? = null,
	showAlphaSlider: Boolean = false,
	dependency: UntisPreferenceDataStore<*>? = null,
	dataStore: UntisPreferenceDataStore<Int>
) {
	val value = remember { mutableStateOf(dataStore.defaultValue) }
	var showDialog by remember { mutableStateOf(false) }

	val scope = rememberCoroutineScope()

	Preference(
		title = title,
		icon = icon,
		trailingContent = { saved, enabled ->
			Box(
				modifier = Modifier
					.disabled(!enabled)
					.size(24.dp)
					.clip(CircleShape)
					.background(MaterialTheme.colorScheme.surface)
					.background(Color(saved))
					.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
			)
		},
		dependency = dependency,
		dataStore = dataStore,
		value = value,
		onClick = { showDialog = true }
	)

	if (showDialog) {
		val presetColors = remember { materialColors.plus(Color(dataStore.defaultValue)) }

		var color by remember { mutableStateOf(Color(value.value)) }
		var selectedPreset by remember { mutableStateOf(presetColors.indexOf(color)) }
		var advanced by remember { mutableStateOf(false) }

		key(advanced) {
			AlertDialog(
				onDismissRequest = { showDialog = false },
				title = title,
				text = {
					if (advanced) {
						selectedPreset = -1
						ColorPicker(
							modifier = Modifier.height(320.dp),
							showAlphaBar = showAlphaSlider,
							initialColor = color,
							onColorChanged = { newColor ->
								color = newColor
							}
						)
					} else {
						LazyVerticalGrid(
							columns = GridCells.Adaptive(48.dp),
							content = {
								items(presetColors.size) { index ->
									Box(
										modifier = Modifier
											.requiredSize(56.dp)
											.padding(4.dp)
											.clip(CircleShape)
											.background(MaterialTheme.colorScheme.surface)
											.background(presetColors[index])
											.border(
												1.dp,
												MaterialTheme.colorScheme.outline,
												shape = CircleShape
											)
											.clickable {
												selectedPreset = index
												color = presetColors[index]
											},
										contentAlignment = Alignment.Center
									) {
										if (selectedPreset == index)
											Icon(
												painter = painterResource(id = R.drawable.all_check),
												contentDescription = null, // TODO: create stringResource(id = R.string.all_selected)
												tint = if (ColorUtils.calculateLuminance(presetColors[index].toArgb()) < 0.5)
													Color.White
												else
													Color.Black
											)
									}
								}
							}
						)
					}
				},
				confirmButton = {
					// Workaround for adding a third button
					Row(
						modifier = Modifier.fillMaxWidth()
					) {
						TextButton(
							onClick = {
								advanced = !advanced
							}) {
							Text(
								if (advanced) "Presets" else "Custom"
							) // TODO: Localize
						}

						Spacer(modifier = Modifier.weight(1f))

						TextButton(onClick = { showDialog = false }) {
							Text(stringResource(id = R.string.all_cancel))
						}

						Spacer(modifier = Modifier.width(8.dp))

						TextButton(
							onClick = {
								showDialog = false
								scope.launch { dataStore.saveValue(color.toArgb()) }
							}) {
							Text(stringResource(id = R.string.all_ok))
						}
					}
				}
			)
		}
	}
}
