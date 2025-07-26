package com.sapuseven.untis.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R

@Composable
fun MessageBubble(
	modifier: Modifier = Modifier,
	colors: MessageBubbleColors = MessageBubbleDefaults.defaultColors(),
	icon: @Composable (() -> Unit)? = null,
	@StringRes messageText: Int?,
	messageTextRaw: String? = null
) {
	AnimatedValueVisibility(value = messageText) {
		CompositionLocalProvider(LocalContentColor provides colors.contentColor) {
			Row(
				modifier = modifier
					.clip(RoundedCornerShape(4.dp))
					.background(colors.containerColor),
				verticalAlignment = Alignment.CenterVertically
			) {
				if (icon != null) {
					Box(
						Modifier
							.align(Alignment.CenterVertically)
							.padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
					) { icon() }
				}

				Column(
					modifier = Modifier
						.weight(1f)
						.padding(vertical = 8.dp)
				) {
					Box(
						Modifier
							.padding(start = 16.dp, end = 16.dp),
						contentAlignment = Alignment.CenterStart
					) {
						Text(
							text = stringResource(id = it),
							style = MaterialTheme.typography.titleMedium
						)
					}

					if (messageTextRaw?.isNotBlank() == true) {
						Box(
							Modifier
								.padding(start = 16.dp, end = 16.dp),
							contentAlignment = Alignment.CenterStart
						) {
							Text(
								text = messageTextRaw,
								style = MaterialTheme.typography.bodyMedium
							)
						}
					}
				}
			}
		}
	}
}

@Immutable
class MessageBubbleColors(
	val containerColor: Color,
	val contentColor: Color,
)

object MessageBubbleDefaults {
	@Composable
	fun defaultColors() = MessageBubbleColors(
		containerColor = MaterialTheme.colorScheme.primary,
		contentColor = MaterialTheme.colorScheme.onPrimary,
	)

	@Composable
	fun primaryColors() = MessageBubbleColors(
		containerColor = MaterialTheme.colorScheme.primaryContainer,
		contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
	)

	@Composable
	fun errorColors() = MessageBubbleColors(
		containerColor = MaterialTheme.colorScheme.errorContainer,
		contentColor = MaterialTheme.colorScheme.onErrorContainer,
	)
}

@Preview
@Composable
fun MessageBubbleErrorPreview() {
	MessageBubble(
		icon = { Icon(painter = painterResource(id = R.drawable.all_error), contentDescription = stringResource(id = R.string.all_error)) },
		messageText = R.string.errormessagedictionary_generic,
		messageTextRaw = "Here can be some additional error details that can even be very long"
	)
}

@Preview
@Composable
fun MessageBubbleErrorColorPreview() {
	MessageBubble(
		icon = { Icon(painter = painterResource(id = R.drawable.all_error), contentDescription = stringResource(id = R.string.all_error)) },
		colors = MessageBubbleDefaults.errorColors(),
		messageText = R.string.errormessagedictionary_generic,
		messageTextRaw = "Here can be some additional error details that can even be very long"
	)
}
