package com.sapuseven.untis.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R

@Composable
fun MessageBubble(
	modifier: Modifier = Modifier,
	icon: @Composable (() -> Unit)? = null,
	@StringRes messageText: Int?,
	messageTextRaw: String? = null
) {
	AnimatedValueVisibility(value = messageText) {
		CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
			Row(
				modifier = modifier
					.clip(RoundedCornerShape(4.dp))
					.background(MaterialTheme.colorScheme.primary),
				verticalAlignment = Alignment.CenterVertically
			) {
				if (icon != null) {
					Box(
						Modifier
							.align(Alignment.CenterVertically)
							//.widthIn(min = 16.dp + IconMinPaddedWidth)
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
							color = MaterialTheme.colorScheme.onPrimary,
							style = MaterialTheme.typography.titleMedium
						)
					}

					messageTextRaw?.let {
						Box(
							Modifier
								.padding(start = 16.dp, end = 16.dp),
							contentAlignment = Alignment.CenterStart
						) {
							Text(
								text = messageTextRaw,
								color = MaterialTheme.colorScheme.onPrimary,
								style = MaterialTheme.typography.bodyMedium
							)
						}
					}
				}
			}
		}
	}
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
