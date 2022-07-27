package com.sapuseven.untis.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledCheckbox(
	checked: Boolean,
	onCheckedChange: ((Boolean) -> Unit)?,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	colors: CheckboxColors = CheckboxDefaults.colors(),
	label: (@Composable () -> Unit),
) {
	Row(
		horizontalArrangement = Arrangement.Start,
		verticalAlignment = Alignment.CenterVertically,
		modifier = if (!enabled) modifier else modifier.then(Modifier
			.clickable(
				interactionSource = interactionSource,
				indication = null,
				role = Role.Checkbox
			) { onCheckedChange?.invoke(!checked) }
		)
	) {
		Checkbox(
			checked = checked,
			onCheckedChange = onCheckedChange,
			enabled = enabled,
			interactionSource = interactionSource,
			colors = colors
		)

		Box(modifier = if (enabled) Modifier else Modifier.alpha(0.38f)) {
			label()
		}
	}
}

@Preview
@Composable
fun LabeledCheckbox_Preview() {
	var checked by remember { mutableStateOf(false) }

	LabeledCheckbox(
		label = { Text("Test") },
		checked = checked,
		onCheckedChange = { checked = it },
		modifier = Modifier.width(200.dp)
	)
}
