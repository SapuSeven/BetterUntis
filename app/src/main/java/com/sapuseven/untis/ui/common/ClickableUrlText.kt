package com.sapuseven.untis.ui.common

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.sapuseven.untis.helpers.TextUtils.annotateUrls

@Composable
fun ClickableUrlText(
	text: String,
	modifier: Modifier = Modifier,
	style: TextStyle = TextStyle.Default.copy(LocalContentColor.current),
	softWrap: Boolean = true,
	overflow: TextOverflow = TextOverflow.Clip,
	maxLines: Int = Int.MAX_VALUE,
	onTextLayout: (TextLayoutResult) -> Unit = {},
	onClick: (String) -> Unit
) {
	val annotatedText = annotateUrls(text)
	ClickableText(
		annotatedText,
		modifier = modifier,
		style = style,
		softWrap = softWrap,
		overflow = overflow,
		maxLines = maxLines,
		onTextLayout = onTextLayout,
		onClick = { clickPos ->
			annotatedText.getStringAnnotations(tag = "url", start = clickPos, end = clickPos)
				.firstOrNull()
				?.let {
					onClick(it.item)
				}
		}
	)
}
