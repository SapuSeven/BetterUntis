package com.sapuseven.untis.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.sapuseven.untis.ui.material.palettes.CorePalette
import com.sapuseven.untis.ui.material.scheme.Scheme

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


fun lightColorSchemeFrom(color: Color): ColorScheme {
	return Scheme.light(color.toArgb()).toColorScheme()
}

fun darkColorSchemeFrom(color: Color): ColorScheme {
	return Scheme.dark(color.toArgb()).toColorScheme()
}
