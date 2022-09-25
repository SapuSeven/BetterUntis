package com.sapuseven.untis.ui.animations

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun fullscreenDialogAnimationEnter(): EnterTransition {
	val density = LocalDensity.current
	val offsetY = { _: Int ->
		with(density) { 40.dp.roundToPx() }
	}

	return slideInVertically(initialOffsetY = offsetY) + fadeIn()
}

@Composable
fun fullscreenDialogAnimationExit(): ExitTransition {
	val density = LocalDensity.current
	val offsetY = { _: Int ->
		with(density) { 40.dp.roundToPx() }
	}

	return slideOutVertically(targetOffsetY = offsetY) + fadeOut()
}
