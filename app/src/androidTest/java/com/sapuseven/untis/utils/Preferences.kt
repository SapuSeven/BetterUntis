package com.sapuseven.untis.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.sapuseven.untis.preferences.DataStorePreferences
import com.sapuseven.untis.ui.preferences.materialColors
import kotlinx.coroutines.runBlocking

fun preferenceWithTheme(
	dataStorePreferences: DataStorePreferences,
	themeColor: Color = materialColors[0],
	darkTheme: Boolean = false
) = runBlocking {
	dataStorePreferences.themeColor.saveValue(themeColor.toArgb())
	dataStorePreferences.darkTheme.saveValue(if (darkTheme) "on" else "off")
	dataStorePreferences
}
