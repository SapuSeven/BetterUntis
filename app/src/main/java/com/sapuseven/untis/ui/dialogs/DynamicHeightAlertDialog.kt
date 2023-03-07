package com.sapuseven.untis.ui.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun DynamicHeightAlertDialog(
	onDismissRequest: () -> Unit,
	confirmButton: @Composable () -> Unit,
	modifier: Modifier = Modifier,
	dismissButton: @Composable (() -> Unit)? = null,
	icon: @Composable (() -> Unit)? = null,
	title: @Composable (() -> Unit)? = null,
	text: @Composable (() -> Unit)? = null,
	shape: Shape = AlertDialogDefaults.shape,
	containerColor: Color = AlertDialogDefaults.containerColor,
	tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
	iconContentColor: Color = AlertDialogDefaults.iconContentColor,
	titleContentColor: Color = AlertDialogDefaults.titleContentColor,
	textContentColor: Color = AlertDialogDefaults.textContentColor
) {
	AlertDialog(
		onDismissRequest = onDismissRequest,
		confirmButton = confirmButton,
		dismissButton = dismissButton,
		icon = icon,
		title = title,
		text = text,
		shape = shape,
		containerColor = containerColor,
		tonalElevation = tonalElevation,
		iconContentColor = iconContentColor,
		titleContentColor = titleContentColor,
		textContentColor = textContentColor,
		properties = DialogProperties(usePlatformDefaultWidth = false),
		modifier = modifier.then(
			Modifier
				.padding(28.dp)
				.fillMaxWidth()
				.wrapContentHeight()
		)
	)
}
