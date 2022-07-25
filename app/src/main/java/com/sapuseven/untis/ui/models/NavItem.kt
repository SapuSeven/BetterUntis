package com.sapuseven.untis.ui.models

import androidx.compose.ui.graphics.painter.Painter
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface

open class NavItem(
	open val id: Int,
	open val icon: Painter,
	open val label: String
)

data class NavItemTimetable(
	override val id: Int,
	override val icon: Painter,
	override val label: String,
	val elementType: TimetableDatabaseInterface.Type
) : NavItem(id, icon, label)

data class NavItemShortcut(
	override val id: Int,
	override val icon: Painter,
	override val label: String,
	val target: Class<*>
) : NavItem(id, icon, label)
