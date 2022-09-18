package com.sapuseven.untis.preferences.preference

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
)

@Composable
fun ColorPreference(
	title: (@Composable () -> Unit),
	icon: (@Composable () -> Unit)? = null,
	showAlphaSlider: Boolean = false,
	defaultValueLabel: String? = null,
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
		val presetColors = remember { materialColors.plus(
			if (defaultValueLabel == null)
				Color(dataStore.defaultValue)
			else
				Color.Black
		) }

		var color by remember { mutableStateOf(Color(value.value)) }
		var selectedPreset by remember { mutableStateOf(presetColors.indexOf(color)) }
		var advanced by remember { mutableStateOf(false) }

		val defaultColor = Color(dataStore.defaultValue)

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
						Column(
							verticalArrangement = Arrangement.spacedBy(16.dp)
						) {
							LazyVerticalGrid(
								columns = GridCells.Adaptive(48.dp),
								userScrollEnabled = false,
								content = {
									items(presetColors.size) { index ->
										ColorBox(
											color = presetColors[index].copy(alpha = color.alpha),
											selected = selectedPreset == index,
											onSelect = {
												selectedPreset = index
												color = it
											}
										)
									}
								}
							)

							defaultValueLabel?.let {
								Row(
									verticalAlignment = Alignment.CenterVertically,
									modifier = Modifier
										.fillMaxWidth()
										.clip(RoundedCornerShape(50))
										.clickable {
											selectedPreset = -1
											color = defaultColor
										}
								) {
									ColorBox(
										color = defaultColor.copy(alpha = color.alpha),
										selected = color == defaultColor,
										onSelect = {
											selectedPreset = -1
											color = it
										}
									)

									Text(
										modifier = Modifier.padding(horizontal = 4.dp),
										style = MaterialTheme.typography.bodyLarge,
										text = defaultValueLabel
									)
								}
							}

							if (showAlphaSlider) {
								Slider(
									value = color.alpha,
									onValueChange = {
										color = color.copy(alpha = it)
									},
									modifier = Modifier.fillMaxWidth()
								)
							}
						}
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
								scope.launch {
									if (color == defaultColor)
										dataStore.clearValue()
									else
										dataStore.saveValue(color.toArgb())
								}
							}) {
							Text(stringResource(id = R.string.all_ok))
						}
					}
				}
			)
		}
	}
}

@Composable
fun ColorBox(
	color: Color,
	selected: Boolean,
	onSelect: (Color) -> Unit
) {
	Box(
		modifier = Modifier
			.requiredSize(56.dp)
			.padding(4.dp)
			.clip(CircleShape)
			.background(MaterialTheme.colorScheme.surface)
			.background(color)
			.border(
				1.dp,
				MaterialTheme.colorScheme.outline,
				shape = CircleShape
			)
			.padding(1.dp)
			.border(
				1.dp,
				color.copy(alpha = 1f),
				shape = CircleShape
			)
			.clickable {
				onSelect(color)
			},
		contentAlignment = Alignment.Center
	) {
		if (selected)
			Icon(
				painter = painterResource(id = R.drawable.all_check),
				contentDescription = null, // TODO: create stringResource(id = R.string.all_selected)
				tint = if (ColorUtils.calculateLuminance(color.toArgb()) < 0.5)
					Color.White
				else
					Color.Black
			)
	}
}
