package com.sapuseven.untis.glance.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.sapuseven.untis.glance.R
import com.sapuseven.untis.glance.ui.WidgetListItemModel
import com.sapuseven.untis.glance.ui.WidgetListView

class TimetableWidget : BaseWidget() {
	private var items by mutableStateOf<List<WidgetListItemModel>?>(null)

	private var elementId by mutableStateOf<Int>(-1)

	@Composable
	override fun Content() {
		AppWidgetTheme() {
			Scaffold(
				titleBar = {
					TitleBar(
						startIcon = ImageProvider(R.drawable.outline_calendar_view_day_24),
						title = "Test profile",
						actions = {
							CircleIconButton(
								imageProvider = ImageProvider(R.drawable.outline_refresh_24),
								contentDescription = "Reload timetable",
								backgroundColor = null,
								onClick = {
									// TODO
								}
							)
						}
					)
					/*WidgetListViewHeader(
						modifier = GlanceModifier
							.fillMaxWidth(),
						headlineContent = "Profile name",//user?.getDisplayedName(LocalContext.current) ?: "(Invalid user)",
						supportingContent = "School name"//user?.userData?.schoolName
					)*/
				}
			) {
				items?.let {
					WidgetListView(
						modifier = GlanceModifier.fillMaxSize(),
						onClickAction = actionRunCallback<ItemActionCallback>(),
						items = it
					)
				} ?: run {
					Box(
						modifier = GlanceModifier.fillMaxSize(),
						contentAlignment = Alignment.Center
					) {
						Text(
							LocalContext.current.getString(R.string.widget_loading),
							style = TextStyle(
								// Body Large
								color = GlanceTheme.colors.onSurface,
								fontSize = 16.sp,
							)
						)
					}
				}
			}
		}
	}

	class ItemActionCallback : ActionCallback {
		override suspend fun onAction(
			context: Context,
			glanceId: GlanceId,
			parameters: ActionParameters
		) {
			Log.d("TimetableWidget", "Item with id $glanceId and params $parameters clicked.")
			// TODO
		}
	}
}
