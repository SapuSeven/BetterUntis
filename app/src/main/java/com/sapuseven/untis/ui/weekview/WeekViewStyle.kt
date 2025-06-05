package com.sapuseven.untis.ui.weekview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

data class WeekViewEventStyle(
	val padding: Int,
	val cornerRadius: Int,
	val lessonNameStyle: TextStyle,
	val lessonInfoStyle: TextStyle,
	val lessonInfoCentered: Boolean,
) {
	companion object {
		fun default(): WeekViewEventStyle = WeekViewEventStyle(
			padding = 2,
			cornerRadius = 4,
			lessonNameStyle = TextStyle.Default.copy(fontWeight = FontWeight.Bold),
			lessonInfoStyle = TextStyle.Default,
			lessonInfoCentered = false,
		)

		fun default(typography: Typography): WeekViewEventStyle = WeekViewEventStyle(
			padding = 2,
			cornerRadius = 4,
			lessonNameStyle = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
			lessonInfoStyle = typography.bodySmall,
			lessonInfoCentered = false,
		)
	}
}

val LocalWeekViewEventStyle = staticCompositionLocalOf<WeekViewEventStyle> {
	error("No WeekViewEventStyle provided")
}

@Composable
fun WeekViewStyle(
	weekViewEventStyle: WeekViewEventStyle = WeekViewEventStyle.default(MaterialTheme.typography),
	content: @Composable () -> Unit
) {
	CompositionLocalProvider(LocalWeekViewEventStyle provides weekViewEventStyle, content = content)
}
