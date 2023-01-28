package com.sapuseven.untis.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.sapuseven.untis.preferences.DataStorePreferences
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.ui.preferences.materialColors
import kotlinx.coroutines.runBlocking

fun preferenceWithThemeColor(
	dataStorePreferences: DataStorePreferences,
	themeColor: Color = materialColors[0]
) = runBlocking {
	dataStorePreferences.themeColor.saveValue(themeColor.toArgb())
	dataStorePreferences
}
