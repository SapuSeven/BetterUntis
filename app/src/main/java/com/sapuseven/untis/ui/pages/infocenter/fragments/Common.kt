package com.sapuseven.untis.ui.pages.infocenter.fragments

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.common.MessageBubble
import com.sapuseven.untis.ui.pages.infocenter.ErrorPreviewParameterProvider
import io.github.fornewid.placeholder.foundation.PlaceholderDefaults
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.foundation.placeholder
import io.github.fornewid.placeholder.foundation.shimmer
import io.github.fornewid.placeholder.material3.color
import io.github.fornewid.placeholder.material3.shimmerHighlightColor

@Composable
internal fun CardLoading() {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.height(96.dp)
			.padding(horizontal = 16.dp, vertical = 8.dp)
			.placeholder(
				visible = true,
				color = PlaceholderDefaults.color(),
				shape = CardDefaults.shape,
				highlight = PlaceholderHighlight.shimmer(PlaceholderDefaults.shimmerHighlightColor())
			)
	) {}
}

@Composable
internal fun InfoCenterLoading() {
	ListItem(
		headlineContent = {
			Text(
				text = "",
				modifier = Modifier
					.fillMaxWidth()
					.placeholder(
						visible = true,
						color = PlaceholderDefaults.color(),
						shape = RoundedCornerShape(4.dp),
						highlight = PlaceholderHighlight.shimmer(PlaceholderDefaults.shimmerHighlightColor())
					)
			)
		},
		supportingContent = {
			Text(
				text = "",
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp)
					.placeholder(
						visible = true,
						color = PlaceholderDefaults.color(),
						shape = RoundedCornerShape(4.dp),
						highlight = PlaceholderHighlight.shimmer(PlaceholderDefaults.shimmerHighlightColor())
					)
			)
		}
	)
}

@Composable
internal fun InfoCenterError(error: Throwable) {
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

@Preview
@Composable
private fun InfoCenterLoadingPreview() {
	InfoCenterLoading()
}

@Preview
@Composable
private fun InfoCenterErrorPreview(
	@PreviewParameter(ErrorPreviewParameterProvider::class)
	error: Throwable,
) {
	InfoCenterError(error)
}
