package com.sapuseven.untis.ui.pages.infocenter.fragments

import android.widget.TextView
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.Attachment
import com.sapuseven.untis.api.model.untis.MessageOfDay
import com.sapuseven.untis.model.rest.Message
import com.sapuseven.untis.ui.dialogs.AttachmentsDialog
import com.sapuseven.untis.ui.pages.infocenter.ErrorPreviewParameterProvider
import io.github.fornewid.placeholder.foundation.PlaceholderDefaults
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.foundation.placeholder
import io.github.fornewid.placeholder.foundation.shimmer
import io.github.fornewid.placeholder.material3.color
import io.github.fornewid.placeholder.material3.shimmerHighlightColor


@Composable
fun InfoCenterMessages(uiState: MessagesUiState) {
	var attachmentsDialog by remember { mutableStateOf<List<Attachment>?>(null) }

	Crossfade(targetState = uiState, label = "InfoCenter Messages Content") { state ->
		LazyColumn(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.fillMaxSize()
		) {
			when (state) {
				MessagesUiState.Loading -> item { InfoCenterLoading() }
				is MessagesUiState.Success -> {
					state.messagesOfDay.fold(
						onSuccess = {
							if (state.isMessagesEmpty) {
								item {
									Text(
										text = stringResource(R.string.infocenter_messages_empty),
										textAlign = TextAlign.Center,
										modifier = Modifier.fillMaxWidth()
									)
								}
							} else {
								items(it) { message ->
									MessageItem(
										subject = message.subject,
										body = message.body,
										attachments = message.attachments
									) { attachments -> attachmentsDialog = attachments }
								}
							}
						},
						onFailure = {
							item {
								InfoCenterError(it)
							}
						}
					)

					state.messages.fold(
						onSuccess = {
							if (!state.isMessagesEmpty) {
								items(it) { message ->
									MessageItem(
										subject = message.subject ?: "",
										body = message.contentPreview ?: "",
										sender = message.sender?.displayName,
										imageUrl = message.sender?.imageUrl,
										attachments = null
									) { attachments -> attachmentsDialog = attachments }
								}
							}
						},
						onFailure = {
							item {
								InfoCenterError(it)
							}
						}
					)
				}
			}
		}
	}

	attachmentsDialog?.let { attachments ->
		AttachmentsDialog(attachments = attachments, onDismiss = { attachmentsDialog = null })
	}
}

@Composable
private fun MessageItem(
	subject: String,
	body: String,
	sender: String? = null,
	imageUrl: String? = null,
	attachments: List<Attachment>? = null,
	onShowAttachments: (List<Attachment>) -> Unit
) {
	val textColor = MaterialTheme.colorScheme.onSurfaceVariant
	val textContent = @Composable {
		Column {
			if (subject.isNotBlank()) {
				Text(
					text = subject,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onSurface
				)
				Text(
					text = body,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			} else {
				AndroidView(factory = { context ->
					TextView(context).apply {
						setTextColor(textColor.toArgb())
					}
				}, update = {
					it.text = HtmlCompat.fromHtml(body, HtmlCompat.FROM_HTML_MODE_COMPACT)
				})
			}
		}
	}

	// TODO: Show message read status
	ListItem(
		headlineContent = sender?.let { {
				Text(
					text = sender,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					color = MaterialTheme.colorScheme.onSurface
				)
			}
		} ?: {
			textContent()
		},
		supportingContent = sender?.let { textContent },
		leadingContent =
			imageUrl?.let {
				{
					val painter = rememberAsyncImagePainter(imageUrl, contentScale = ContentScale.Crop)

					Image(
						painter = painter,
						contentScale = ContentScale.Crop,
						contentDescription = null,
						modifier = Modifier
							.size(48.dp)
							.clip(shape = CircleShape)
							.placeholder(
								visible = painter.state !is AsyncImagePainter.State.Success,
								color = PlaceholderDefaults.color(),
								highlight = PlaceholderHighlight.shimmer(PlaceholderDefaults.shimmerHighlightColor())
							)
					)
				}
			},
		trailingContent = if (attachments?.isNotEmpty() == true) {
			{
				// TODO: Show message sent time
				IconButton(onClick = {
					onShowAttachments(attachments)
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

	data class Success(
		val messagesOfDay: Result<List<MessageOfDay>>,
		val messages: Result<List<Message>>
	) : MessagesUiState {
		constructor(messagesOfDay: List<MessageOfDay>, messages: List<Message>) : this(
			Result.success(messagesOfDay),
			Result.success(messages)
		)

		val isMessagesEmpty: Boolean
			get() = messagesOfDay.getOrDefault(emptyList()).isEmpty()
				&& messages.getOrDefault(emptyList()).isEmpty()
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
		uiState = MessagesUiState.Success(Result.failure(error), Result.failure(error))
	)
}

/*@Preview
@Composable
private fun InfoCenterMessagesContentPreview(
	@PreviewParameter(InfoCenterMessagesOfDayPreviewParameterProvider::class)
	messagesOfDay: List<MessageOfDay>,
	@PreviewParameter(InfoCenterMessagesPreviewParameterProvider::class)
	messages: List<Message>,
) {
	InfoCenterMessages(
		uiState = MessagesUiState.Success(Result.success(messagesOfDay), Result.success(messages))
	)
}*/
