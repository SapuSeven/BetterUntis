package com.sapuseven.untis.glance.widget

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent

abstract class BaseWidget : GlanceAppWidget() {
	// TODO: Load color scheme from preferences
	// TODO: Provide an interface for loading timetable data

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		// Load data needed to render the AppWidget.
		// Use `withContext` to switch to another thread for long running
		// operations.

		provideContent {
			// create your AppWidget here
			Content()
		}
	}

	@Composable
	abstract fun Content()

	@Composable
	fun AppWidgetTheme(
		//userId: Long,
		content: @Composable (/*colorSchemeDark: ColorScheme, colorSchemeLight: ColorScheme*/) -> Unit
	) {
		val useDynamicColors = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
		val userColorScheme = GlanceTheme.colors

		GlanceTheme(
			colors = if (useDynamicColors)
				GlanceTheme.colors
			else
				userColorScheme,
			content = content
		)
		/*val context = LocalContext.current

		val themeColorPref = context.intDataStore(
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

	/*@Composable
	fun Content2() {
		val prefs = currentState<Preferences>()
		val userId = prefs[longPreferencesKey(PREFERENCE_KEY_LONG_USER)] ?: -1
		elementId = prefs[intPreferencesKey(PREFERENCE_KEY_INT_ELEMENT_ID)] ?: -1

		val userDatabase = UserDatabase.getInstance(LocalContext.current)
		val user = userDatabase.userDao().getById(userId)


	}

	fun setData(data: List<WidgetListItemModel>) {
		items = data
	}*/
}
