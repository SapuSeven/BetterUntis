package com.sapuseven.untis.ui.models

import androidx.compose.ui.graphics.painter.Painter
import com.sapuseven.untis.api.model.untis.enumeration.ElementType

open class NavItem(
	open val icon: Painter,
	open val label: String
)

data class NavItemTimetable(
	override val icon: Painter,
	override val label: String,
	val elementType: ElementType
) : NavItem(icon, label)

@Deprecated("Shortcuts are not supported with compose-navigation. Use NavItemNavigation instead")
data class NavItemShortcut(
	override val icon: Painter,
	override val label: String,
	val target: Class<*>? //TODO("Maybe not the best option to make this nullable")
) : NavItem(icon, label)

data class NavItemNavigation(
	override val icon: Painter,
	override val label: String,
	val route: Any
) : NavItem(icon, label)
