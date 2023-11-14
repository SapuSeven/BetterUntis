package com.sapuseven.untis.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@Composable
fun AbbreviatedText(
	text: String,
	abbreviatedText: String,
	modifier: Modifier = Modifier,
	color: Color = Color.Unspecified,
	fontSize: TextUnit = TextUnit.Unspecified,
	fontStyle: FontStyle? = null,
	fontWeight: FontWeight? = null,
	fontFamily: FontFamily? = null,
	letterSpacing: TextUnit = TextUnit.Unspecified,
	textDecoration: TextDecoration? = null,
	textAlign: TextAlign? = null,
	lineHeight: TextUnit = TextUnit.Unspecified,
	overflow: TextOverflow = TextOverflow.Ellipsis,
	softWrap: Boolean = true,
	style: TextStyle = LocalTextStyle.current
) {
	var isOverflowing by remember { mutableStateOf(false) }

	Text(
		text = if (isOverflowing) abbreviatedText else text,
		modifier = modifier,
		color = color,
		fontSize = fontSize,
		fontStyle = fontStyle,
		fontWeight = fontWeight,
		fontFamily = fontFamily,
		letterSpacing = letterSpacing,
		textDecoration = textDecoration,
		textAlign = textAlign,
		lineHeight = lineHeight,
		overflow = overflow,
		softWrap = softWrap,
		minLines = 1,
		maxLines = 1,
		style = style,
		onTextLayout = { textLayoutResult ->
			isOverflowing = isOverflowing or textLayoutResult.hasVisualOverflow
		}
	)
}

@Composable
@Preview
fun AbbreviatedTextTest() {
	Column(modifier = Modifier
		.fillMaxWidth()
		.background(Color.White)) {
		AbbreviatedText(
			"This is a very long text",
			"This is text",
			modifier = Modifier.width(200.dp)
		)
		AbbreviatedText(
			"This is a very long text",
			"This is text",
			modifier = Modifier.width(100.dp)
		)
	}
}
