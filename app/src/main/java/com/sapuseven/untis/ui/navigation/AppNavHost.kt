package com.sapuseven.untis.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sapuseven.untis.ui.activities.login.Login
import com.sapuseven.untis.ui.activities.logindatainput.LoginDataInput

@Composable
fun AppNavHost(
	modifier: Modifier = Modifier,
	navController: NavHostController,
	startDestination: String = NavigationItem.Login.route,
) {
	NavHost(
		modifier = modifier,
		navController = navController,
		startDestination = startDestination
	) {
		composable(NavigationItem.Login.route) { Login(navController) }
		composable(NavigationItem.LoginDataInput.route) { LoginDataInput(navController) }
	}
}
