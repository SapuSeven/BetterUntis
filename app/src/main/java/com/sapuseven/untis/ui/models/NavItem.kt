package com.sapuseven.untis.ui.models

import androidx.compose.ui.graphics.painter.Painter
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
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
	val elementType: ElementType
) : NavItem(id, icon, label)

@Deprecated("Shortcuts are not supported with compose-navigation. Use NavItemNavigation instead")
data class NavItemShortcut(
	override val id: Int,
	override val icon: Painter,
	override val label: String,
	val target: Class<*>? //TODO("Maybe not the best option to make this nullable")
) : NavItem(id, icon, label)

data class NavItemNavigation(
	override val id: Int,
	override val icon: Painter,
	override val label: String,
	val route: Any
) : NavItem(id, icon, label)
