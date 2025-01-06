package com.sapuseven.untis.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sapuseven.untis.ui.functional.bottomInsets

@Deprecated("Please use Modifier.verticalScroll(rememberScrollState()) directly")
@Composable
fun VerticalScrollColumn(content: @Composable ColumnScope.() -> Unit) {
	Column(
		modifier = Modifier
			.verticalScroll(rememberScrollState())
			.bottomInsets(),
		content = content
	)
}
