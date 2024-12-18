package com.sapuseven.untis.mappers

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.viewModelScope
import com.sapuseven.compose.protostore.ui.preferences.convertRangeToPair
import com.sapuseven.untis.annotations.UserScope
import com.sapuseven.untis.components.UserManager
import com.sapuseven.untis.data.databases.entities.UserDao
import com.sapuseven.untis.data.settings.model.UserSettings
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.DateTimeUtils
import com.sapuseven.untis.modules.ThemeManager
import com.sapuseven.untis.scope.UserScopeManager
import com.sapuseven.untis.ui.activities.settings.SettingsRepository
import com.sapuseven.untis.ui.navigation.AppNavigator
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.joda.time.DateTimeConstants
import javax.inject.Inject

class TimetableMapper @Inject constructor(
	private val repository: SettingsRepository
) {
	private var debug = ""

	private val settingsFlow = MutableStateFlow<UserSettings?>(null)
	private val scope = GlobalScope	// TODO: How to get the scope here?

	init {
		Log.d("TimetableViewModel", "mapper init: $repository")
		scope.launch {
			repository.getSettings().collect { userSettings ->
				settingsFlow.value = userSettings

				// All properties that are based on preferences are set here
				Log.d("TimetableViewModel", "mapper assign: $repository")
				debug = userSettings.timetablePersonalTimetable
			}
		}
	}

	public suspend fun map(): String {
		waitForSettings()

		return "it works: $debug!"
	}

	private suspend fun waitForSettings() {
		settingsFlow.filterNotNull().first()
	}

	/*private suspend fun prepareItems(
		items: List<TimegridItem>
	): List<TimegridItem> {
		val newItems = mergeItems(items.mapNotNull { item ->
			if (item.periodData.isCancelled() && preferences.timetableHideCancelled.getValue()) return@mapNotNull null

			if (preferences.timetableSubstitutionsIrregular.getValue()) {
				item.periodData.apply {
					forceIrregular =
						classes.find { it.id != it.orgId } != null || teachers.find { it.id != it.orgId } != null || subjects.find { it.id != it.orgId } != null || rooms.find { it.id != it.orgId } != null || preferences.timetableBackgroundIrregular.getValue() && item.periodData.element.backColor != UNTIS_DEFAULT_COLOR
				}
			}
			item
		})
		colorItems(newItems)
		return newItems
	}

	private fun mergeItems(items: List<TimegridItem>): List<TimegridItem> {
		val days = user.timeGrid.days
		val itemGrid: Array<Array<MutableList<TimegridItem>>> =
			Array(days.size) { Array(days.maxByOrNull { it.units.size }!!.units.size) { mutableListOf() } }
		val leftover: MutableList<TimegridItem> = mutableListOf()

		// TODO: Check if the day from the Untis API is always an english string
		val firstDayOfWeek =
			DateTimeConstants.MONDAY //DateTimeFormat.forPattern("EEE").withLocale(Locale.ENGLISH).parseDateTime(days.first().day).dayOfWeek

		// Put all items into a two dimensional array depending on day and hour
		items.forEach { item ->
			val startDateTime = item.periodData.element.startDateTime.toLocalDateTime()
			val endDateTime = item.periodData.element.endDateTime.toLocalDateTime()

			val day = endDateTime.dayOfWeek - firstDayOfWeek

			if (day < 0 || day >= days.size) return@forEach

			val thisUnitStartIndex = days[day].units.indexOfFirst {
				it.startTime.time == startDateTime.toString(DateTimeUtils.tTimeNoSeconds())
			}

			val thisUnitEndIndex = days[day].units.indexOfFirst {
				it.endTime.time == endDateTime.toString(DateTimeUtils.tTimeNoSeconds())
			}

			if (thisUnitStartIndex != -1 && thisUnitEndIndex != -1) itemGrid[day][thisUnitStartIndex].add(
				item
			)
			else leftover.add(item)
		}

		val newItems = mutableListOf<TimegridItem>()
		newItems.addAll(leftover) // Add items that didn't fit inside the timegrid. These will always be single lessons.
		itemGrid.forEach { unitsOfDay ->
			unitsOfDay.forEachIndexed { unitIndex, items ->
				items.forEach {
					var i = 1
					while (unitIndex + i < unitsOfDay.size && it.mergeWith(unitsOfDay[unitIndex + i])) i++
				}

				newItems.addAll(items)
			}
		}
		return newItems
	}*/

	/*private suspend fun colorItems(
		items: List<TimegridItem>
	) {
		val regularColor = weekViewPreferences.backgroundRegular.value
		val regularPastColor = weekViewPreferences.backgroundRegularPast.value
		val examColor = weekViewPreferences.backgroundExam.value
		val examPastColor = weekViewPreferences.backgroundExamPast.value
		val cancelledColor = weekViewPreferences.backgroundCancelled.value
		val cancelledPastColor = weekViewPreferences.backgroundCancelledPast.value
		val irregularColor = weekViewPreferences.backgroundIrregular.value
		val irregularPastColor = weekViewPreferences.backgroundIrregularPast.value

		val useDefault = preferences.schoolBackground.getValue()

		items.forEach { item ->
			val defaultColor = android.graphics.Color.parseColor(item.periodData.element.backColor)
			val defaultTextColor =
				android.graphics.Color.parseColor(item.periodData.element.foreColor)

			item.color = when {
				item.periodData.isExam() -> if (useDefault.contains("exam")) defaultColor else examColor
				item.periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultColor else cancelledColor
				item.periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultColor else irregularColor
				else -> if (useDefault.contains("regular")) defaultColor else regularColor
			}

			item.pastColor = when {
				item.periodData.isExam() -> if (useDefault.contains("exam")) defaultColor.darken(
					0.25f
				) else examPastColor

				item.periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultColor.darken(
					0.25f
				) else cancelledPastColor

				item.periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultColor.darken(
					0.25f
				) else irregularPastColor

				else -> if (useDefault.contains("regular")) defaultColor.darken(0.25f) else regularPastColor
			}

			item.textColor = when {
				item.periodData.isExam() -> if (useDefault.contains("exam")) defaultTextColor else colorOn(
					Color(examColor)
				).toArgb()

				item.periodData.isCancelled() -> if (useDefault.contains("cancelled")) defaultTextColor else colorOn(
					Color(cancelledColor)
				).toArgb()

				item.periodData.isIrregular() -> if (useDefault.contains("irregular")) defaultTextColor else colorOn(
					Color(irregularColor)
				).toArgb()

				else -> if (useDefault.contains("regular")) defaultTextColor else colorOn(
					Color(
						regularColor
					)
				).toArgb()
			}
		}
	}

	private fun colorOn(color: Color): Color {
		return when (color.copy(alpha = 1f)) {
			colorScheme.primary -> colorScheme.onPrimary
			colorScheme.secondary -> colorScheme.onSecondary
			colorScheme.tertiary -> colorScheme.onTertiary
			else -> if (ColorUtils.calculateLuminance(color.toArgb()) < 0.5) Color.White else Color.Black
		}.copy(alpha = color.alpha)
	}*/
}
