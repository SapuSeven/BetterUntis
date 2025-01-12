package com.sapuseven.untis.ui.pages.infocenter.fragments

import android.widget.TextView
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.Attachment
import com.sapuseven.untis.api.model.untis.MessageOfDay
import com.sapuseven.untis.ui.dialogs.AttachmentsDialog

@Composable
fun InfoCenterMessages(messages: Result<List<MessageOfDay>>?) {
	var attachmentsDialog by remember { mutableStateOf<List<Attachment>?>(null) }

	ItemList(
		itemResult = messages,
		itemRenderer = { MessageItem(it) { attachments -> attachmentsDialog = attachments } },
		itemsEmptyMessage = R.string.infocenter_messages_empty,
	)

	attachmentsDialog?.let { attachments ->
		AttachmentsDialog(
			attachments = attachments,
			onDismiss = { attachmentsDialog = null }
		)
	}
}

@Composable
private fun MessageItem(
	item: MessageOfDay,
	onShowAttachments: (List<Attachment>) -> Unit
) {
	val textColor = MaterialTheme.colorScheme.onSurfaceVariant

	ListItem(
		headlineContent = { Text(item.subject) },
		supportingContent = {
			AndroidView(
				factory = { context ->
					TextView(context).apply {
						setTextColor(textColor.toArgb())
					}
				},
				update = {
					it.text = HtmlCompat.fromHtml(item.body, HtmlCompat.FROM_HTML_MODE_COMPACT)
				}
			)
		},
		trailingContent = if (item.attachments.isNotEmpty()) {
			{
				IconButton(onClick = {
					onShowAttachments(item.attachments)
				}) {
					Icon(
						painter = painterResource(id = R.drawable.infocenter_attachments),
						contentDescription = stringResource(id = R.string.infocenter_messages_attachments)
					)
				}
			}
		} else null
	)
}
