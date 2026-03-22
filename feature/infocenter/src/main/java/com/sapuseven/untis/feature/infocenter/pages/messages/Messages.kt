package com.sapuseven.untis.feature.infocenter.pages.messages

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sapuseven.untis.core.model.messages.DirectMessage
import com.sapuseven.untis.core.model.messages.MessageOfDay
import com.sapuseven.untis.core.model.timetable.Attachment
import com.sapuseven.untis.core.ui.dialogs.AttachmentsDialog
import com.sapuseven.untis.feature.infocenter.InfoCenterViewModel
import com.sapuseven.untis.feature.infocenter.R
import com.sapuseven.untis.feature.infocenter.pages.CardLoading
import com.sapuseven.untis.feature.infocenter.pages.InfoCenterError
import kotlinx.datetime.LocalDateTime


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun InfoCenterMessages(viewModel: InfoCenterViewModel = hiltViewModel()) {
	val uiState by viewModel.messagesState.collectAsStateWithLifecycle()
	var attachmentsDialog by remember { mutableStateOf<List<Attachment>?>(null) }

	BackHandler(enabled = uiState is MessagesUiState.Details) {
		viewModel.onMessageDismiss()
	}

	SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
		AnimatedContent(
			targetState = uiState,
			label = "InfoCenter Messages Content",
			transitionSpec = {
				fadeIn() togetherWith fadeOut()
			}
		) { state ->
			when (state) {
				MessagesUiState.Loading -> {
					LazyColumn(
						horizontalAlignment = Alignment.CenterHorizontally,
						modifier = Modifier.fillMaxSize()
					) {
						items(3) { CardLoading() }
					}
				}

				is MessagesUiState.Success -> {
					LazyColumn(
						horizontalAlignment = Alignment.CenterHorizontally,
						modifier = Modifier.fillMaxSize()
					) {
						items(state.errors) {
							InfoCenterError(it)
						}

						if (state.messages.isEmpty()) {
							item {
								Text(
									text = stringResource(R.string.feature_infocenter_messages_empty),
									textAlign = TextAlign.Center,
									modifier = Modifier.fillMaxWidth()
								)
							}
						} else {
							items(state.messages) { message ->
								Card(
									modifier = Modifier
										.padding(horizontal = 16.dp, vertical = 8.dp)
										.sharedBounds(
											sharedContentState = rememberSharedContentState(key = "${message.id}-bounds"),
											animatedVisibilityScope = this@AnimatedContent,
											clipInOverlayDuringTransition = OverlayClip(
												CardDefaults.shape
											),
											resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
										)
										.clip(CardDefaults.shape),
									onClick = { viewModel.onMessageClicked(message) }
								) {
									MessagePreview(
										subject = message.subject
											?: "(no subject)", // TODO proper fallback
										body = message.content,
										time = message.dateTime,
										unread = message.unread,
										attachments = message.attachments,
									) { attachmentsDialog = message.attachments }
								}
							}
						}
					}
				}

				is MessagesUiState.Details -> {
					MessageDetailsCard(
						messageState = state.detailsState,
						canReply = false, // TODO not yet implemented - selectedMessage?.isReplyAllowed == true,
						canDelete = false, // TODO not yet implemented - selectedMessage?.allowMessageDeletion == true,
						onReply = { viewModel.onMessageReply() },
						onDelete = { viewModel.onMessageDelete() },
					)
				}
			}
		}
	}

	attachmentsDialog?.let { attachments ->
		AttachmentsDialog(attachments = attachments, onDismiss = { attachmentsDialog = null })
	}
}

sealed interface Message {
	val id: String
	val subject: String?
	val content: String?
	val attachments: List<Attachment>?
	val dateTime: LocalDateTime?
	val unread: Boolean
		get() = false

	data class Day(val messageOfDay: MessageOfDay) : Message {
		override val id = "day-${messageOfDay.id}"
		override val subject = messageOfDay.subject
		override val content = messageOfDay.body
		override val attachments = messageOfDay.attachments
		override val dateTime = null
	}

	data class Direct(val directMessage: DirectMessage) : Message {
		override val id = "direct-${directMessage.id}"
		override val subject = directMessage.subject
		override val content = directMessage.body
		override val attachments = directMessage.attachments
		override val dateTime = directMessage.timestamp
		override val unread = directMessage.unread
		val sender = directMessage.sender
	}
}

sealed interface MessagesUiState {
	data object Loading : MessagesUiState

	data class Success(
		val errors: List<Throwable>,
		val messages: List<Message>,
	) : MessagesUiState

	data class Details(
		val detailsState: MessageDetailsState,
	) : MessagesUiState
}

sealed interface MessageDetailsState {
	val message: Message

	data class Loading(override val message: Message) : MessageDetailsState
	data class Success(override val message: Message, val fullContent: String?) :
		MessageDetailsState

	data class Error(override val message: Message, val error: String) : MessageDetailsState
}
