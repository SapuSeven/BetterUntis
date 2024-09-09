package com.sapuseven.compose.protostore.ui.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.google.protobuf.MessageLite
import com.sapuseven.compose.protostore.R
import com.sapuseven.compose.protostore.data.SettingsRepository
import com.sapuseven.compose.protostore.ui.utils.colorpicker.ColorPicker
import com.sapuseven.compose.protostore.ui.utils.disabled
import kotlinx.coroutines.CoroutineScope
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
fun <Model : MessageLite, ModelBuilder : MessageLite.Builder> ColorPreference(
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit)? = null,
	//supportingContent: @Composable ((value: Float, enabled: Boolean) -> Unit)? = null,
	leadingContent: (@Composable () -> Unit)? = null,
	//trailingContent: @Composable ((value: Float, enabled: Boolean) -> Unit)? = null,
	settingsRepository: SettingsRepository<Model, ModelBuilder>,
	value: (Model) -> Int,
	scope: CoroutineScope = rememberCoroutineScope(),
	enabledCondition: (Model) -> Boolean = { true },
	highlight: Boolean = false,
	showAlphaSlider: Boolean = false,
	defaultValueLabel: String? = null,
	onValueChange: (ModelBuilder.(value: Int) -> Unit)? = null,
) {
	var dialogValue by remember { mutableIntStateOf(0) }
	var showDialog by remember { mutableStateOf(false) }

	Preference(
		title = title,
		summary = summary,
		leadingContent = leadingContent,
		trailingContent = { currentValue, enabled ->
			Box(
				modifier = Modifier
					.disabled(!enabled)
					.size(24.dp)
					.clip(CircleShape)
					.background(MaterialTheme.colorScheme.surface)
					.background(Color(currentValue))
					.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
			)
		},
		settingsRepository = settingsRepository,
		value = value,
		scope = scope,
		enabledCondition = enabledCondition,
		highlight = highlight,
		onClick = {
			dialogValue = it
			showDialog = true
		}
	)

	if (showDialog) {
		val defaultColor = Color(value(settingsRepository.getSettingsDefaults()))

		val color = Color(dialogValue);
		val presetColors = remember {
			materialColors.plus(
				if (defaultValueLabel == null)
					defaultColor
				else
					Color.Black
			)
		}

		var selectedPreset by remember { mutableIntStateOf(presetColors.indexOf(color)) }
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
								dialogValue = newColor.toArgb()
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
												dialogValue = it.toArgb()
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
											dialogValue = defaultColor.toArgb()
										}
								) {
									ColorBox(
										color = defaultColor,
										selected = color == defaultColor,
										onSelect = {
											selectedPreset = -1
											dialogValue = it.toArgb()
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
										dialogValue = color.copy(alpha = it).toArgb()
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
							Text(stringResource(id = R.string.dialog_cancel))
						}

						Spacer(modifier = Modifier.width(8.dp))

						TextButton(
							onClick = {
								showDialog = false
								scope.launch {
									/*if (color == defaultColor)
										dataStore.clearValue()
									else
										dataStore.saveValue(color.toArgb())*/
									settingsRepository.updateSettings {
										onValueChange?.invoke(this, color.toArgb())
									}
								}
							}) {
							Text(stringResource(id = R.string.dialog_ok))
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
				painter = painterResource(id = R.drawable.colorpicker_check),
				contentDescription = null, // TODO: localize stringResource(id = R.string.all_selected)
				tint = if (ColorUtils.calculateLuminance(color.toArgb()) < 0.5)
					Color.White
				else
					Color.Black
			)
	}
}
