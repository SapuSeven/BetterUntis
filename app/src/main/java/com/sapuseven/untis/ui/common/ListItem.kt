package com.sapuseven.untis.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.sapuseven.untis.R

@Composable
fun ListItem_TwoLine(
	line1: String,
	line2: String,
	onClick: () -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onClick() }
			.padding(
				vertical = dimensionResource(id = R.dimen.margin_listitem_text_vertical),
				horizontal = dimensionResource(id = R.dimen.margin_listitem_text)
			)
	) {
		Text(
			text = line1,
			style = MaterialTheme.typography.bodyLarge,
			fontWeight = FontWeight.Bold
		)
		Text(
			text = line2,
			style = MaterialTheme.typography.bodyMedium
		)
	}
}

@Preview
@Composable
fun ListItem_TwoLine_Preview() {
	ListItem_TwoLine(line1 = "Line 1", line2 = "Line 2", onClick = {})
}
