package com.sapuseven.untis.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.sapuseven.compose.protostore.ui.preferences.materialColors

@Composable
fun AppTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	darkThemeOled: Boolean = false,
	themeColor: Color? = null,
	content: @Composable () -> Unit
) {
	val colorScheme = generateColorScheme(
		context = LocalContext.current,
		dynamicColor = themeColor == null,
		themeColor = themeColor ?: materialColors[0],
		darkTheme = darkTheme,
		darkThemeOled = darkThemeOled
	)

	MaterialTheme(
		colorScheme = colorScheme,
		content = content
	)
}
