package com.sapuseven.untis.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.sapuseven.untis.ui.material.scheme.Scheme

fun generateColorScheme(
	context: Context,
	dynamicColor: Boolean,
	themeColor: Color,
	darkTheme: Boolean,
	darkThemeOled: Boolean
): ColorScheme = when {
	dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
		if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
	}
	darkTheme -> Scheme.dark(themeColor.toArgb()).toColorScheme()
	else -> Scheme.light(themeColor.toArgb()).toColorScheme()
}.run {
	if (darkTheme && darkThemeOled)
		copy(background = Color.Black, surface = Color.Black)
	else
		this
}

fun Scheme.toColorScheme(): ColorScheme =
	ColorScheme(
		primary = Color(primary),
		onPrimary = Color(onPrimary),
		primaryContainer = Color(primaryContainer),
		onPrimaryContainer = Color(onPrimaryContainer),
		inversePrimary = Color(inversePrimary),
		secondary = Color(secondary),
		onSecondary = Color(onSecondary),
		secondaryContainer = Color(secondaryContainer),
		onSecondaryContainer = Color(onSecondaryContainer),
		tertiary = Color(tertiary),
		onTertiary = Color(onTertiary),
		tertiaryContainer = Color(tertiaryContainer),
		onTertiaryContainer = Color(onTertiaryContainer),
		background = Color(background),
		onBackground = Color(onBackground),
		surface = Color(surface),
		onSurface = Color(onSurface),
		surfaceVariant = Color(surfaceVariant),
		onSurfaceVariant = Color(onSurfaceVariant),
		surfaceTint = Color(primary),
		inverseSurface = Color(inverseSurface),
		inverseOnSurface = Color(inverseOnSurface),
		error = Color(error),
		onError = Color(onError),
		errorContainer = Color(errorContainer),
		onErrorContainer = Color(onErrorContainer),
		outline = Color(outline),
		outlineVariant = Color(outlineVariant),
		scrim = Color(scrim),
	)
