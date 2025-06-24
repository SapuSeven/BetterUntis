package com.sapuseven.untis.ui.pages.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun <T : Any> SettingsCategory(
	key: T,
	title: (@Composable () -> Unit),
	summary: (@Composable () -> Unit),
	icon: (@Composable () -> Unit) = {},
	isFirst: Boolean = false,
	isLast: Boolean = false,
	navController: NavController
) {
	val headlineTextStyle = MaterialTheme.typography.titleLarge
	val supportingTextStyle = MaterialTheme.typography.bodyMedium

	val innerRadius = 4.dp
	val outerRadius = 24.dp

	Surface(
		modifier = Modifier
			.padding(horizontal = 16.dp, vertical = 1.dp)
			.clip(
				RoundedCornerShape(
					if (isFirst) outerRadius else innerRadius,
					if (isFirst) outerRadius else innerRadius,
					if (isLast) outerRadius else innerRadius,
					if (isLast) outerRadius else innerRadius
				)
			),
		tonalElevation = 3.dp
	) {
		ListItem(
			headlineContent = {
				CompositionLocalProvider(
					LocalTextStyle provides LocalTextStyle.current.merge(headlineTextStyle),
					content = title
				)
			},
			supportingContent = {
				CompositionLocalProvider(
					LocalTextStyle provides LocalTextStyle.current.merge(supportingTextStyle),
					content = summary
				)
			},
			leadingContent = icon,
			modifier = Modifier
				.clickable {
					navController.navigate(key)
				}
				.padding(horizontal = 8.dp, vertical = 4.dp)
		)
	}
}
