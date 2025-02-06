package com.sapuseven.untis.ui.pages.settings

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun <T: Any> PreferenceScreen(
	key: T,
	title: (@Composable () -> Unit),
	icon: (@Composable () -> Unit) = {},
	navController: NavController
) {
	ListItem(
		headlineContent = title,
		leadingContent = icon,
		modifier = Modifier.clickable {
			navController.navigate(key)
		}
	)
}
