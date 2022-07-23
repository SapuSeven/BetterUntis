package com.sapuseven.untis.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun LabeledSwitch(
	checked: Boolean,
	onCheckedChange: ((Boolean) -> Unit)?,
	modifier: Modifier = Modifier,
	thumbContent: (@Composable () -> Unit)? = null,
	enabled: Boolean = true,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	colors: SwitchColors = SwitchDefaults.colors(),
	label: (@Composable () -> Unit),
) {
	Row(
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically,
		modifier = if (!enabled) modifier else modifier.then(Modifier
			.clickable(
				interactionSource = interactionSource,
				indication = null,
				role = Role.Switch
			) { onCheckedChange?.invoke(!checked) }
		)
	) {
		Box(modifier = if (enabled) Modifier else Modifier.alpha(0.38f)) {
			label()
		}

		Switch(
			checked = checked,
			onCheckedChange = onCheckedChange,
			thumbContent = thumbContent,
			enabled = enabled,
			interactionSource = interactionSource,
			colors = colors
		)
	}
}

@Preview
@Composable
fun LabeledSwitch_Preview() {
	var checked by remember { mutableStateOf(false) }

	LabeledSwitch(
		label = { Text("Test") },
		checked = checked,
		onCheckedChange = { checked = it },
		modifier = Modifier.width(200.dp)
	)
}
