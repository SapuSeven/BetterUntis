package com.sapuseven.untis.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

@Composable
fun Modifier.disabled(disabled: Boolean = true): Modifier =
	if (disabled) this.alpha(0.38f) else this

@Composable
fun Modifier.conditional(
	condition: Boolean,
	modifier: @Composable Modifier.() -> Modifier
): Modifier =
	if (condition) then(modifier(this)) else this
