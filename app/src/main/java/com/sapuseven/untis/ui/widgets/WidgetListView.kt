package com.sapuseven.untis.ui.widgets

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.unit.ColorProvider
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import com.sapuseven.untis.widgets.toGlanceTextStyle

@Composable
fun WidgetListViewHeader(
	modifier: GlanceModifier = GlanceModifier,
	dayColorScheme: ColorScheme,
	nightColorScheme: ColorScheme,
	headlineText: String,
	supportingText: String? = null,
) {
	val primary = ColorProvider(
		day = dayColorScheme.primary,
		night = nightColorScheme.primary,
	)

	val onPrimary = ColorProvider(
		day = dayColorScheme.onPrimary,
		night = nightColorScheme.onPrimary,
	)

	WidgetListItem(
		modifier = modifier,
		headlineText = headlineText,
		supportingText = supportingText,
		surfaceColor = primary,
		textColor = onPrimary,
		/*trailingContent = {
			Image(
				provider = ImageProvider(R.drawable.base_appwidget_reload),
				contentDescription = context.resources.getString(R.string.all_reload),
				modifier = GlanceModifier.padding(start = 16.dp)
			)
		}*/
	)
}

@Composable
fun WidgetListView(
	modifier: GlanceModifier,
	dayColorScheme: ColorScheme,
	nightColorScheme: ColorScheme,
	items: List<WidgetListItemModel>,
	onClickAction: Action,
) {
	val surface = ColorProvider(
		day = dayColorScheme.surface,
		night = nightColorScheme.surface,
	)

	val onSurface = ColorProvider(
		day = dayColorScheme.onSurface,
		night = nightColorScheme.onSurface,
	)

	LazyColumn(
		modifier = modifier
	) {
		items(items) {
			WidgetListItem(
				// TODO: The current version (alpha05) has a bug which prevents click events from children to be registered; see https://issuetracker.google.com/issues/242397933
				// TODO: Use actionStartActivity on timetable item click, otherwise show reload action
				modifier = GlanceModifier.clickable(onClickAction),
				leadingContent = it.leadingContent,
				headlineText = it.headlineText,
				supportingText = it.supportingText,
				surfaceColor = surface,
				textColor = onSurface
			)
		}
	}
}

@Composable
private fun WidgetListItem(
	modifier: GlanceModifier = GlanceModifier,
	headlineText: String,
	supportingText: String? = null,
	surfaceColor: ColorProvider,
	textColor: ColorProvider,
	typography: Typography = MaterialTheme.typography,
	leadingContent: @Composable ((surfaceColor: ColorProvider, textColor: ColorProvider) -> Unit)? = null,
	trailingContent: @Composable (() -> Unit)? = null,
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.background(surfaceColor)
			.padding(horizontal = 16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		leadingContent?.let {
			Box(
				contentAlignment = Alignment.Center,
				modifier = GlanceModifier
					.padding(end = 16.dp)
			) {
				leadingContent(surfaceColor, textColor)
			}
		}

		Column(
			modifier = GlanceModifier
				.defaultWeight()
				.padding(vertical = 8.dp)
		) {
			Text(
				headlineText,
				style = typography.bodyLarge.toGlanceTextStyle(textColor)
			)
			supportingText?.let {
				Text(
					supportingText,
					style = typography.bodyMedium.toGlanceTextStyle(textColor)
				)
			}
		}

		trailingContent?.invoke()
	}
}

data class WidgetListItemModel(
	val headlineText: String,
	val supportingText: String,
	val leadingContent: @Composable ((surfaceColor: ColorProvider, textColor: ColorProvider) -> Unit)?
)
