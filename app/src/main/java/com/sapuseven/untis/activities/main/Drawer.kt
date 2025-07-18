package com.sapuseven.untis.activities.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.timetable.PeriodElement
import com.sapuseven.untis.ui.navigation.AppRoutes


@Composable
fun DrawerItems(
	disableTypeSelection: Boolean = false,
	displayedElement: PeriodElement? = null,
	onTimetableClick: (item: NavItemTimetable) -> Unit,
	onNavigationClick: (item: NavItemNavigation) -> Unit,
) {
	val navItemsElementTypes = listOf(
		NavItemTimetable(
			icon = painterResource(id = R.drawable.all_classes),
			label = stringResource(id = R.string.all_classes),
			elementType = ElementType.CLASS
		),
		NavItemTimetable(
			icon = painterResource(id = R.drawable.all_teachers),
			label = stringResource(id = R.string.all_teachers),
			elementType = ElementType.TEACHER
		),
		NavItemTimetable(
			icon = painterResource(id = R.drawable.all_rooms),
			label = stringResource(id = R.string.all_rooms),
			elementType = ElementType.ROOM
		),
	)

	val navItemsNavigation = listOf(
		NavItemNavigation(
			icon = painterResource(id = R.drawable.all_infocenter),
			label = stringResource(id = R.string.activity_title_info_center),
			route = AppRoutes.InfoCenter
		),
		NavItemNavigation(
			icon = painterResource(id = R.drawable.all_search_rooms),
			label = stringResource(id = R.string.activity_title_free_rooms),
			route = AppRoutes.RoomFinder
		),
		NavItemNavigation(
			icon = painterResource(id = R.drawable.all_settings),
			label = stringResource(id = R.string.activity_title_settings),
			route = AppRoutes.Settings
		)
	)

	navItemsElementTypes.forEach { item ->
		NavigationDrawerItem(
			icon = { Icon(item.icon, contentDescription = null) },
			label = { Text(item.label) },
			selected = !disableTypeSelection && item.elementType == displayedElement?.type,
			onClick = { onTimetableClick(item) },
			modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
		)
	}

	DrawerDivider()

	navItemsNavigation.forEach { item ->
		NavigationDrawerItem(
			icon = { Icon(item.icon, contentDescription = null) },
			label = { Text(item.label) },
			selected = false,
			onClick = { onNavigationClick(item) },
			modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
		)
	}
}

@Composable
fun DrawerDivider() {
	HorizontalDivider(
		color = MaterialTheme.colorScheme.outline,
		modifier = Modifier.padding(vertical = 8.dp)
	)
}

@Composable
fun DrawerText(text: String) {
	Text(
		text = text,
		style = MaterialTheme.typography.labelMedium,
		modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 8.dp)
	)
}

open class NavItem(
	open val icon: Painter,
	open val label: String
)

data class NavItemTimetable(
	override val icon: Painter,
	override val label: String,
	val elementType: ElementType
) : NavItem(icon, label)

data class NavItemNavigation(
	override val icon: Painter,
	override val label: String,
	val route: Any
) : NavItem(icon, label)
