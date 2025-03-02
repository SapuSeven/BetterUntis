package com.sapuseven.untis.widgets

import androidx.compose.ui.unit.TextUnit
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider

internal fun androidx.compose.ui.text.TextStyle.toGlanceTextStyle(
	color: ColorProvider = ColorProvider(this.color),
	fontSize: TextUnit? = this.fontSize,
	fontWeight: FontWeight? = this.fontWeight?.toGlanceFontWeight(),
	fontStyle: FontStyle? = this.fontStyle?.toGlanceFontStyle(),
	textAlign: TextAlign = this.textAlign.toGlanceTextAlign(),
	textDecoration: TextDecoration? = this.textDecoration?.toGlanceTextDecoration()
): TextStyle =
	run {
		TextStyle(
			color = color,
			fontSize = fontSize,
			fontWeight = fontWeight,
			fontStyle = fontStyle,
			textAlign = textAlign,
			textDecoration = textDecoration
		)
	}

internal fun androidx.compose.ui.text.font.FontWeight.toGlanceFontWeight(): FontWeight =
	when (weight) {
		500 -> FontWeight.Medium
		700 -> FontWeight.Bold
		else -> FontWeight.Normal
	}

internal fun androidx.compose.ui.text.font.FontStyle.toGlanceFontStyle(): FontStyle = when (this) {
	androidx.compose.ui.text.font.FontStyle.Italic -> FontStyle.Italic
	else -> FontStyle.Normal
}

internal fun androidx.compose.ui.text.style.TextAlign.toGlanceTextAlign(): TextAlign = when (this) {
	androidx.compose.ui.text.style.TextAlign.Right -> TextAlign.Right
	androidx.compose.ui.text.style.TextAlign.Center -> TextAlign.Center
	androidx.compose.ui.text.style.TextAlign.Start -> TextAlign.Start
	androidx.compose.ui.text.style.TextAlign.End -> TextAlign.End
	else -> TextAlign.Left
}

internal fun androidx.compose.ui.text.style.TextDecoration.toGlanceTextDecoration(): TextDecoration =
	when (this) {
		androidx.compose.ui.text.style.TextDecoration.Underline -> TextDecoration.Underline
		androidx.compose.ui.text.style.TextDecoration.LineThrough -> TextDecoration.LineThrough
		androidx.compose.ui.text.style.TextDecoration.combine(
			listOf(
				androidx.compose.ui.text.style.TextDecoration.Underline,
				androidx.compose.ui.text.style.TextDecoration.LineThrough
			)
		) -> TextDecoration.combine(
			listOf(
				TextDecoration.Underline,
				TextDecoration.LineThrough
			)
		)
		else -> TextDecoration.None
	}
