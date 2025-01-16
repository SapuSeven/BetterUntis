package com.sapuseven.untis.ui.pages.infocenter.fragments

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R
import com.sapuseven.untis.ui.common.MessageBubble

@Composable
internal fun InfoCenterLoading() {
	LinearProgressIndicator(
		strokeCap = StrokeCap.Square,
		modifier = Modifier.fillMaxWidth()
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
