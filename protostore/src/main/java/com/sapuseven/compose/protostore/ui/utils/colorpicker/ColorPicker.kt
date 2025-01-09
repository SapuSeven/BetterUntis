package com.sapuseven.compose.protostore.ui.utils.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * Classic Color Picker Component that shows a HSV representation of a color, with a Hue Bar on the right,
 * Alpha Bar on the bottom and the rest of the area covered with an area with saturation value touch area.
 *
 * @param modifier modifiers to set to this color picker.
 * @param showAlphaBar whether or not to show the bottom alpha bar on the color picker.
 * @param initialColor the initial color to set on the picker.
 * @param onColorChanged callback that is triggered when the color changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPicker(
	modifier: Modifier = Modifier,
	showAlphaBar: Boolean = true,
	initialColor: Color,
	onColorChanged: (Color) -> Unit
) {
	var color by remember { mutableStateOf(HsvColor.from(initialColor)) }
	var colorHex by remember { mutableStateOf('#' + initialColor.toArgb().toColorHex()) }

	fun onColorChanged(newColor: HsvColor, updateColorHex: Boolean = true) {
		color = newColor
		if (updateColorHex)
			colorHex = '#' + newColor.toColor().toArgb().toColorHex()
		onColorChanged(newColor.toColor())
	}

	Column(modifier = modifier) {
		Row(
			modifier = Modifier.weight(1f)
		) {
			val paddingBetweenBars = 8.dp
			Column(modifier = Modifier.weight(0.8f)) {
				SaturationValueArea(
					modifier = Modifier.weight(1f),
					cornerRadius = CornerRadius(with(LocalDensity.current) { 4.dp.toPx() }),
					currentColor = color,
					onSaturationValueChanged = { saturation, value ->
						onColorChanged(color.copy(saturation = saturation, value = value))
					}
				)
				if (showAlphaBar) {
					Spacer(modifier = Modifier.height(paddingBetweenBars))
					HorizontalGradientSlider(
						brush = Brush.linearGradient(listOf(color.copy(alpha = 1f).toColor(), color.copy(alpha = 0f).toColor())),
						value = 1 - color.alpha,
						onValueChanged = { onColorChanged(color.copy(alpha = 1 - it)) },
						thumbColor = color.copy(alpha = 1f).toColor(),
					)
				}
			}
			Spacer(modifier = Modifier.width(paddingBetweenBars))

			VerticalGradientSlider(
				brush = Brush.linearGradient(listOf(
					Color.Red,
					Color.Yellow,
					Color.Green,
					Color.Cyan,
					Color.Blue,
					Color.Magenta,
					Color.Red
				)),
				value = color.hue,
				onValueChanged = { onColorChanged(color.copy(hue = it)) },
				valueRange = 0f..360f,
				thumbColor = color.copy(alpha = 1f, saturation = 1f, value = 1f).toColor(),
			)
		}

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(top = 16.dp)
		) {
			Box(modifier = Modifier
				.padding(end = 16.dp)
				.weight(1f)
				.height(56.dp)
				.clip(RoundedCornerShape(4.dp))
				.background(MaterialTheme.colorScheme.surface)
				.background(color.toColor())
				.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
			)

			TextField(
				modifier = Modifier.width(128.dp),
				value = colorHex,
				onValueChange = { newColor ->
					if (newColor.matches(Regex("^#[0-9a-fA-F]{0,8}$"))) {
						colorHex = newColor
						newColor.replace("#", "").toUIntOrNull(16)?.let {
							onColorChanged(HsvColor.from(Color(it.toInt())), false)
						}
					}
				},
				label = { Text("ARGB Hex") }, // TODO: Localize
				singleLine = true,
				textStyle = LocalTextStyle.current.copy(
					fontFamily = FontFamily.Monospace
				)
			)
		}
	}
}

private fun Int.toColorHex(): String = this.toUInt().toString(16).padStart(8, '0')
