package com.sapuseven.untis.ui.pages.infocenter.fragments

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign


@Composable
internal fun <T> ItemList(
	items: List<T>?,
	itemRenderer: @Composable (T) -> Unit,
	@StringRes itemsEmptyMessage: Int,
) {
	if (items == null) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier.fillMaxSize()
		) {
			CircularProgressIndicator()
		}
	} else if (items.isEmpty()) {
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
