package com.sapuseven.untis.ui.pages.infocenter.fragments

import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun InfoCenterMessages(uiState: MessagesUiState) {
	var attachmentsDialog by remember { mutableStateOf<List<Attachment>?>(null) }
	var selectedMessage by remember { mutableStateOf<Message?>(null) }

	Crossfade(targetState = uiState, label = "InfoCenter Messages Content") { state ->
		SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
			LazyColumn(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier.fillMaxSize()
			) {
				when (state) {
					MessagesUiState.Loading -> items(3) { CardLoading() }
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
										Card(
											modifier = Modifier
												.padding(horizontal = 16.dp, vertical = 8.dp)
												.clip(CardDefaults.shape)
										) {
											MessagePreview(
												subject = message.subject,
												body = message.body,
												attachments = message.attachments,
												onShowAttachments = { attachmentsDialog = message.attachments },
											)
										}
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
										AnimatedVisibility(
											visible = message != selectedMessage,
											enter = expandVertically(),
											exit = shrinkVertically(),
											modifier = Modifier.animateItem()
										) {
											Card(
												modifier = Modifier
													.padding(horizontal = 16.dp, vertical = 8.dp)
													.sharedBounds(
														sharedContentState = rememberSharedContentState(key = "${message.id}-bounds"),
														animatedVisibilityScope = this@AnimatedVisibility,
														clipInOverlayDuringTransition = OverlayClip(CardDefaults.shape)
													)
													.clip(CardDefaults.shape),
												onClick = {
													selectedMessage = message
												}
											) {
												MessagePreview(
													modifier = Modifier
														.sharedElement(
															sharedContentState = rememberSharedContentState(key = message.id),
															animatedVisibilityScope = this@AnimatedVisibility,
														),
													subject = message.subject ?: "",
													body = message.contentPreview ?: "",
													sender = message.sender?.displayName,
													imageUrl = message.sender?.imageUrl,
													time = message.sentDateTime,
													read = message.isMessageRead ?: true,
													attachments = null
												)
											}
										}
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

			MessageDetails(
				message = selectedMessage
			)

			BackHandler(selectedMessage != null) {
				selectedMessage = null
			}
		}
	}

	attachmentsDialog?.let { attachments ->
		AttachmentsDialog(attachments = attachments, onDismiss = { attachmentsDialog = null })
	}
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.MessageDetails(
	message: Message?,
	modifier: Modifier = Modifier
) {
	AnimatedContent(
		modifier = modifier.fillMaxSize(),
		targetState = message,
		transitionSpec = {
			fadeIn() togetherWith fadeOut()
		},
		label = "MessageDetails"
	) { targetMessage ->
		Box(
			modifier = Modifier.fillMaxSize()
		) {
			if (targetMessage != null) {
				Surface(modifier = Modifier.fillMaxSize()) {
					Column {
						Card(
							modifier = Modifier
								.padding(horizontal = 16.dp, vertical = 8.dp)
								.sharedBounds(
									sharedContentState = rememberSharedContentState(key = "${targetMessage.id}-bounds"),
									animatedVisibilityScope = this@AnimatedContent,
									clipInOverlayDuringTransition = OverlayClip(CardDefaults.shape)
								)
								.clip(CardDefaults.shape)
						) {
							Column {
								MessagePreview(
									modifier = Modifier
										.sharedElement(
											sharedContentState = rememberSharedContentState(key = targetMessage.id),
											animatedVisibilityScope = this@AnimatedContent,
										),
									subject = targetMessage.subject ?: "",
									sender = targetMessage.sender?.displayName,
									imageUrl = targetMessage.sender?.imageUrl,
									time = targetMessage.sentDateTime,
									read = targetMessage.isMessageRead ?: true,
									attachments = null
								)

								Text(
									modifier = Modifier
										.fillMaxWidth()
										.padding(horizontal = 16.dp)
										.verticalScroll(rememberScrollState())
										.placeholder(
											visible = true,
											color = PlaceholderDefaults.color(),
											shape = RoundedCornerShape(4.dp),
											highlight = PlaceholderHighlight.shimmer(
												PlaceholderDefaults.shimmerHighlightColor()
											)
										),
									text = ""
								)

								Row(
									modifier = Modifier
										.fillMaxWidth()
										.padding(horizontal = 16.dp, vertical = 12.dp),
									horizontalArrangement = Arrangement.spacedBy(12.dp)
								) {
									FilledTonalButton(
										modifier = Modifier.weight(1f),
										onClick = {}
									) {
										Text(text = "Reply")
									}
									FilledTonalButton(
										modifier = Modifier.weight(1f),
										onClick = {}
									) {
										Text(text = "Delete")
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

@Composable
private fun MessagePreview(
	subject: String,
	modifier: Modifier = Modifier,
	body: String? = null,
	sender: String? = null,
	imageUrl: String? = null,
	time: LocalDateTime? = null,
	read: Boolean = true,
	attachments: List<Attachment>? = null,
	onShowAttachments: () -> Unit = {}
) {
	val textColor = MaterialTheme.colorScheme.onSurfaceVariant
	val textContent = @Composable {
		Column {
			if (subject.isNotBlank()) {
				Text(
					text = subject,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					fontWeight = if (!read) FontWeight.Bold else null,
					color = MaterialTheme.colorScheme.onSurface
				)
				body?.let {
					Text(
						text = body,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						fontWeight = if (!read) FontWeight.Bold else null,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			} else {
				body?.let {
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
	}

	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 12.dp),
	) {
		imageUrl?.let {
			val painter = rememberAsyncImagePainter(imageUrl, contentScale = ContentScale.Crop)

			Image(
				painter = painter,
				contentScale = ContentScale.Crop,
				contentDescription = null,
				modifier = Modifier
					.padding(end = 16.dp)
					.size(40.dp)
					.clip(shape = CircleShape)
					.placeholder(
						visible = painter.state !is AsyncImagePainter.State.Success,
						color = PlaceholderDefaults.color(),
						highlight = PlaceholderHighlight.shimmer(PlaceholderDefaults.shimmerHighlightColor())
					)
			)
		}

		Column(
			modifier = Modifier
				.weight(1f)
		) {
			sender?.let {
				Text(
					text = sender,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface
				)
			}
			textContent()
		}

		Column(
			horizontalAlignment = Alignment.End,
			modifier = Modifier
				.padding(start = 16.dp)
		) {
			time?.let {
				Text(
					text = if (time.toLocalDate().equals(LocalDate.now()))
						time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
					else
						time.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			attachments?.let {
				IconButton(onClick = {
					onShowAttachments()
				}) {
					Icon(
						painter = painterResource(id = R.drawable.infocenter_attachments),
						contentDescription = stringResource(id = R.string.infocenter_messages_attachments)
					)
				}
			}
		}
	}
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
