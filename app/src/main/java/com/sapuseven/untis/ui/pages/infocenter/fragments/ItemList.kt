package com.sapuseven.untis.ui.pages.infocenter.fragments

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.common.MessageBubble


internal fun <T> LazyListScope.itemList(
	itemResult: Result<List<T>>?,
	itemRenderer: @Composable (T) -> Unit,
	isLoading: Boolean = itemResult == null,
	@StringRes itemsEmptyMessage: Int,
	modifier: Modifier = Modifier
) {
	item {
		AnimatedVisibility(isLoading) {
			Box(
				contentAlignment = Alignment.TopCenter,
				modifier = modifier
			) {
				LinearProgressIndicator(
					modifier = Modifier.fillMaxWidth(),
					strokeCap = StrokeCap.Square
				)
			}
		}
	}

	item {
		AnimatedVisibility(itemResult?.isFailure == true) {
			itemResult?.onFailure { error ->
				MessageBubble(
					modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
					icon = {
						Icon(
							painter = painterResource(id = R.drawable.all_error),
							contentDescription = stringResource(id = R.string.all_error)
						)
					},
					messageText = R.string.errormessagedictionary_generic,
					messageTextRaw = error.message
				)
			}
		}
	}

	itemResult?.onSuccess { items ->
		if (items.isEmpty()) {
			item {
				Box(
					contentAlignment = Alignment.Center,
					modifier = modifier
				) {
					Text(
						text = stringResource(id = itemsEmptyMessage),
						textAlign = TextAlign.Center,
						modifier = Modifier.fillMaxWidth()
					)
				}
			}
		} else {
			items(items) {
				itemRenderer(it)
			}
		}
	}
}
