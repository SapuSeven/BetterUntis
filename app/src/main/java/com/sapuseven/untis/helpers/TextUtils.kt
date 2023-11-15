package com.sapuseven.untis.helpers

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.substring
import androidx.compose.ui.text.withStyle

object TextUtils {
	fun isNullOrEmpty(obj: Any?): Boolean {
		return obj?.toString()?.length ?: 0 == 0
	}

	@Composable
	fun annotateUrls(
		text: String,
		urlColor: Color = MaterialTheme.colorScheme.primary
	): AnnotatedString = buildAnnotatedString {
		val urlRegex = """(?:https?:\/\/|www\.)[\w-@:%_+.~#?&/=]+""".toRegex()

		var startIndex = 0
		while (startIndex < text.length) {
			// find the next match
			val match = urlRegex.find(text, startIndex)

			if (match == null) {
				// no more matches - append remaining text and return
				append(text.substring(startIndex))
				return@buildAnnotatedString
			}

			if (match.range.start > startIndex) {
				// matching url found with preceding text - append text first
				append(text.substring(startIndex, match.range.start))
			}

			// append matched url
			appendUrl(match.value, urlColor)

			// set new start index to the end of the matched url
			startIndex = match.range.endInclusive + 1
		}
	}

	private fun AnnotatedString.Builder.appendUrl(url: String, color: Color) {
		pushStringAnnotation("url", url)
		withStyle(style = SpanStyle(color = color)) {
			append(url)
		}
		pop()
	}
}
