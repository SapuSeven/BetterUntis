package com.sapuseven.untis.ui.navigation

enum class Screen {
	TIMETABLE,
	LOGIN,
	LOGIN_DATA_INPUT,
}
sealed class NavigationItem(val route: String) {
	data object Timetable : NavigationItem(Screen.TIMETABLE.name)
	data object Login : NavigationItem(Screen.LOGIN.name)
	data object LoginDataInput : NavigationItem(Screen.LOGIN_DATA_INPUT.name)
}
