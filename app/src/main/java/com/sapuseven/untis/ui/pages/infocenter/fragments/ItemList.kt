package com.sapuseven.untis.ui.pages.infocenter.fragments

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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


@Composable
internal fun <T> ItemList(
	itemResult: Result<List<T>>?,
	itemRenderer: @Composable (T) -> Unit,
	@StringRes itemsEmptyMessage: Int,
) {
	AnimatedVisibility(itemResult == null) {
		Box(
			contentAlignment = Alignment.TopCenter,
			modifier = Modifier.fillMaxSize()
		) {
			LinearProgressIndicator(
				modifier = Modifier.fillMaxWidth(),
				strokeCap = StrokeCap.Square
			)
		}
	}

	AnimatedVisibility(itemResult?.isFailure == true) {
		itemResult?.onFailure { error ->

			MessageBubble(
				modifier = Modifier
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

	itemResult?.onSuccess { items ->
		if (items.isEmpty()) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier.fillMaxSize()
			) {
				Text(
					text = stringResource(id = itemsEmptyMessage),
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth()
				)
			}
		} else {
			LazyColumn(
				modifier = Modifier.fillMaxSize()
			) {
				items(items) {
					itemRenderer(it)
				}
			}
		}
	}
}
