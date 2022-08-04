package com.sapuseven.untis.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

fun Modifier.disabled(disabled: Boolean = true): Modifier =
	if (disabled)
		this.alpha(0.38f)
	else
		this
