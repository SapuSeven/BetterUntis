package com.sapuseven.untis.glance.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

@Composable
fun WidgetListViewHeader(
	modifier: GlanceModifier = GlanceModifier,
	headlineContent: String,
	supportingContent: String? = null,
) {
	GlanceTheme.colors

	WidgetListItem(
		modifier = modifier,
		headlineContent = headlineContent,
		supportingContent = supportingContent,
		surfaceColor = GlanceTheme.colors.primary,
		textColor = GlanceTheme.colors.onPrimary,
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
	items: List<WidgetListItemModel>,
	onClickAction: Action,
) {
	LazyColumn(
		modifier = modifier
	) {
		items(items) {
			WidgetListItem(
				// TODO: Use actionStartActivity on timetable item click, otherwise show reload action
				modifier = GlanceModifier.clickable(onClickAction),
				leadingContent = it.leadingContent,
				headlineContent = it.headlineContent,
				supportingContent = it.supportingContent,
				surfaceColor = GlanceTheme.colors.surface,
				textColor = GlanceTheme.colors.onSurface
			)
		}
	}
}

@Composable
private fun WidgetListItem(
	modifier: GlanceModifier = GlanceModifier,
	headlineContent: String,
	supportingContent: String? = null,
	surfaceColor: ColorProvider,
	textColor: ColorProvider,
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
				headlineContent,
				style = TextStyle(
					// Body Large
					color = GlanceTheme.colors.onSurface,
					fontSize = 16.sp,
				)
			)
			supportingContent?.let {
				Text(
					supportingContent,
					style = TextStyle(
						// Body Medium
						color = GlanceTheme.colors.onSurfaceVariant,
						fontSize = 14.sp,
					)
				)
			}
		}

		trailingContent?.invoke()
	}
}

data class WidgetListItemModel(
	val headlineContent: String,
	val supportingContent: String,
	val leadingContent: @Composable ((surfaceColor: ColorProvider, textColor: ColorProvider) -> Unit)?
)
