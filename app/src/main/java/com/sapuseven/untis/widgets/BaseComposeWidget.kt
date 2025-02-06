package com.sapuseven.untis.widgets

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.glance.GlanceId
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import com.sapuseven.untis.ui.widgets.WidgetListItemModel

open class BaseComposeWidget : GlanceAppWidget() {
	companion object {
		const val PREFERENCE_KEY_LONG_USER = "userId"
		const val PREFERENCE_KEY_INT_ELEMENT_ID = "elementId"
		const val PREFERENCE_KEY_STRING_ELEMENT_TYPE = "elementType"
	}

	private var items by mutableStateOf<List<WidgetListItemModel>?>(null)

	private var elementId by mutableIntStateOf(-1)

	@Composable
	fun AppWidgetTheme(
		userId: Long,
		content: @Composable (colorSchemeDark: ColorScheme, colorSchemeLight: ColorScheme) -> Unit
	) {
		val context = LocalContext.current

		// TODO
		/*val themeColorPref = context.intDataStore(
			userId,
			"preference_theme_color",
			defaultValue = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
				with(LocalContext.current) {
					resources.getColor(android.R.color.system_accent1_500, theme)
				}
			else
				materialColors[0].toArgb()
		)

		val themeColor = runBlocking { Color(themeColorPref.getValue()) }

		content(
			generateColorScheme(
				context = context,
				themeColor == Color(themeColorPref.defaultValue),
				themeColor,
				darkTheme = true,
				false // Ignore for now
			),
			generateColorScheme(
				context = context,
				themeColor == Color(themeColorPref.defaultValue),
				themeColor,
				darkTheme = false,
				false // Ignore for now
			)
		)*/
	}

	@Composable
	/*override fun Content() {
		val prefs = currentState<Preferences>()
		val userId = prefs[longPreferencesKey(PREFERENCE_KEY_LONG_USER)] ?: -1
		elementId = prefs[intPreferencesKey(PREFERENCE_KEY_INT_ELEMENT_ID)] ?: -1

		val userDatabase = UserDatabase.getInstance(LocalContext.current)
		val user = userDatabase.userDao().getById(userId)

		AppWidgetTheme(userId) { colorSchemeDark, colorSchemeLight ->
			val onSurface = ColorProvider(
				day = colorSchemeLight.onSurface,
				night = colorSchemeDark.onSurface,
			)

			Column(
				modifier = GlanceModifier
					.fillMaxSize()
					.background(
						day = colorSchemeLight.surface,
						night = colorSchemeDark.surface
					)
					.appWidgetBackground()
			) {
				WidgetListViewHeader(
					modifier = GlanceModifier
						.fillMaxWidth(),
					dayColorScheme = colorSchemeLight,
					nightColorScheme = colorSchemeDark,
					headlineContent = user?.getDisplayedName(LocalContext.current) ?: "(Invalid user)",
					supportingContent = user?.userData?.schoolName
				)


				items?.let {
					WidgetListView(
						modifier = GlanceModifier
							.fillMaxWidth()
							.defaultWeight(),
						dayColorScheme = colorSchemeLight,
						nightColorScheme = colorSchemeDark,
						onClickAction = actionRunCallback<TimetableItemActionCallback>(),
						items = it
					)
				} ?: run {
					Box(
						modifier = GlanceModifier
							.fillMaxWidth()
							.defaultWeight(),
						contentAlignment = Alignment.Center
					) {
						Text(
							LocalContext.current.getString(R.string.loading),
							style = MaterialTheme.typography.bodyLarge.toGlanceTextStyle(onSurface)
						)
					}
				}
			}
		}
	}*/

	fun setData(data: List<WidgetListItemModel>) {
		items = data
	}

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		TODO("Not yet implemented")
	}
}
