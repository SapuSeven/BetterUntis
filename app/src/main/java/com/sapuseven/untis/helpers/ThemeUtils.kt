package com.sapuseven.untis.helpers

import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sapuseven.compose.protostore.ui.preferences.materialColors
import com.sapuseven.untis.ui.material.scheme.Scheme
import com.sapuseven.untis.ui.theme.animated
import com.sapuseven.untis.ui.theme.toColorScheme


private fun setSystemUiColor(
	systemUiController: SystemUiController,
	color: Color = Color.Transparent,
	darkIcons: Boolean = color.luminance() > 0.5f
) {
	systemUiController.run {
		setSystemBarsColor(
			color = color, darkIcons = darkIcons
		)

		setNavigationBarColor(
			color = color, darkIcons = darkIcons
		)
	}
}

@Composable
fun AppTheme(
	darkTheme: ThemeMode = ThemeMode.FollowSystem,
	darkThemeOled: Boolean = false,
	themeColor: Color? = null,
	systemUiController: SystemUiController? = rememberSystemUiController(),
	content: @Composable () -> Unit
) {
	val defaultThemeColor = materialColors[0]
	val isDarkTheme = when (darkTheme) {
		ThemeMode.AlwaysLight -> false
		ThemeMode.AlwaysDark -> true
		ThemeMode.FollowSystem -> isSystemInDarkTheme()
	}
	Log.d("AppTheme", "Theme mode: $darkTheme, isDarkTheme: $isDarkTheme")

	val colorScheme = when {
		themeColor == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
			val context = LocalContext.current
			if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
		}

		isDarkTheme -> Scheme.dark((themeColor ?: defaultThemeColor).toArgb()).toColorScheme()
		else -> Scheme.light((themeColor ?: defaultThemeColor).toArgb()).toColorScheme()
	}.run {
		if (isDarkTheme && darkThemeOled)
			copy(background = Color.Black, surface = Color.Black)
		else
			this
	}

	val darkIcons = colorScheme.background.luminance() > .5f
	systemUiController?.let {
		setSystemUiColor(it, Color.Transparent, darkIcons)
	}

	MaterialTheme(
		colorScheme = colorScheme.animated(),
		content = content
	)
}

enum class ThemeMode {
	AlwaysLight,
	AlwaysDark,
	FollowSystem
}
