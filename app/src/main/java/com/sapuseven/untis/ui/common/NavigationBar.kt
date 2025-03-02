package com.sapuseven.untis.ui.common;

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.sapuseven.untis.ui.functional.bottomInsets

@Composable
fun NavigationBarInset(
	modifier: Modifier = Modifier,
	containerColor: Color = NavigationBarDefaults.containerColor,
	contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
	tonalElevation: Dp = NavigationBarDefaults.Elevation,
	content: @Composable RowScope.() -> Unit
) {
	Box(
		modifier = Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(tonalElevation))
	) {
		NavigationBar(
			modifier = modifier.bottomInsets(),
			containerColor = Color.Transparent,
			contentColor = contentColor,
			tonalElevation = tonalElevation,
			content = content,
		)
	}
}
