package com.sapuseven.untis.ui.animations

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
