package com.sapuseven.untis.ui.pages.infocenter.fragments

import android.widget.TextView
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.Attachment
import com.sapuseven.untis.api.model.untis.MessageOfDay
import com.sapuseven.untis.ui.dialogs.AttachmentsDialog
import com.sapuseven.untis.ui.pages.infocenter.ErrorPreviewParameterProvider
import com.sapuseven.untis.ui.pages.infocenter.InfoCenterPreviewParameterProvider

@Composable
fun InfoCenterMessages(uiState: MessagesUiState) {
	var attachmentsDialog by remember { mutableStateOf<List<Attachment>?>(null) }

	Crossfade(targetState = uiState, label = "InfoCenter Messages Content") { state ->
		when (state) {
			MessagesUiState.Loading -> InfoCenterLoading()
			is MessagesUiState.Success -> {
				state.messages.fold(
					onSuccess = {
						if (state.isEmpty) {
							Text(
								text = stringResource(R.string.infocenter_messages_empty),
								textAlign = TextAlign.Center,
								modifier = Modifier.fillMaxWidth()
							)
						} else {
							LazyColumn(
								horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()
							) {
								items(it) {
									MessageItem(it) { attachments -> attachmentsDialog = attachments }
								}
							}
						}
					},
					onFailure = { InfoCenterError(it) }
				)
			}
		}
	}

	attachmentsDialog?.let { attachments ->
		AttachmentsDialog(attachments = attachments, onDismiss = { attachmentsDialog = null })
	}
}

@Composable
private fun MessageItem(
	item: MessageOfDay,
	onShowAttachments: (List<Attachment>) -> Unit
) {
	val textColor = MaterialTheme.colorScheme.onSurfaceVariant
	val textContent = @Composable {
		AndroidView(factory = { context ->
			TextView(context).apply {
				setTextColor(textColor.toArgb())
			}
		}, update = {
			it.text = HtmlCompat.fromHtml(item.body, HtmlCompat.FROM_HTML_MODE_COMPACT)
		})
	}

	ListItem(
		headlineContent = { if (item.subject.isNotBlank()) Text(item.subject) else textContent() },
		supportingContent = { if (item.subject.isNotBlank()) textContent() },
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

sealed interface MessagesUiState {
	data object Loading : MessagesUiState

	data class Success(val messages: Result<List<MessageOfDay>>) : MessagesUiState {
		constructor(messages: List<MessageOfDay>) : this(Result.success(messages))

		val isEmpty: Boolean get() = messages.getOrDefault(emptyList()).isEmpty()
	}
}

@Preview
@Composable
private fun InfoCenterMessagesLoadingPreview() {
	InfoCenterMessages(
		uiState = MessagesUiState.Loading
	)
}

@Preview
@Composable
private fun InfoCenterMessagesErrorPreview(
	@PreviewParameter(ErrorPreviewParameterProvider::class)
	error: Throwable,
) {
	InfoCenterMessages(
		uiState = MessagesUiState.Success(Result.failure(error))
	)
}

@Preview
@Composable
private fun InfoCenterMessagesContentPreview(
	@PreviewParameter(InfoCenterPreviewParameterProvider::class)
	messages: List<MessageOfDay>,
) {
	InfoCenterMessages(
		uiState = MessagesUiState.Success(Result.success(messages))
	)
}
