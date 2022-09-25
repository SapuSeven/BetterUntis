package com.sapuseven.untis.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SmallCircularProgressIndicator(
	modifier: Modifier = Modifier,
	color: Color = ProgressIndicatorDefaults.circularColor
) {
	CircularProgressIndicator(
		color = color,
		strokeWidth = 3.dp,
		modifier = modifier.size(24.dp)
	)
}
