package com.sapuseven.untis.feature.infocenter.pages.messages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.core.model.messages.DirectMessage
import com.sapuseven.untis.core.model.messages.MessageParticipant
import com.sapuseven.untis.core.model.timetable.Attachment
import com.sapuseven.untis.core.ui.common.MessageBubble
import com.sapuseven.untis.core.ui.common.MessageBubbleDefaults
import com.sapuseven.untis.feature.infocenter.R
import io.github.fornewid.placeholder.foundation.PlaceholderDefaults
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.foundation.placeholder
import io.github.fornewid.placeholder.foundation.shimmer
import io.github.fornewid.placeholder.material3.color
import io.github.fornewid.placeholder.material3.shimmerHighlightColor
import kotlinx.datetime.LocalDateTime


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SharedTransitionScope.MessageDetailsCard(
	messageState: MessageDetailsState?,
	modifier: Modifier = Modifier,
	canReply: Boolean = false,
	canDelete: Boolean = false,
	onReply: () -> Unit,
	onDelete: () -> Unit,
) {
	val uriHandler = LocalUriHandler.current

	AnimatedContent(
		modifier = modifier.fillMaxSize(),
		targetState = messageState,
		label = "MessageDetails"
	) { targetState ->
		Box(
			modifier = Modifier.fillMaxSize()
		) {
			targetState?.let {
				Surface(modifier = Modifier.fillMaxSize()) {
					Column {
						Card(
							modifier = Modifier
								.padding(horizontal = 16.dp, vertical = 8.dp)
								.sharedBounds(
									sharedContentState = rememberSharedContentState(key = "${targetState.message.id}-bounds"),
									animatedVisibilityScope = this@AnimatedContent,
									clipInOverlayDuringTransition = OverlayClip(CardDefaults.shape),
									resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
								)
								.clip(CardDefaults.shape)
						) {
							Column {
								val sender = (targetState.message as? Message.Direct)?.sender
								// Title without body to reuse the same layout
								MessagePreview(
									subject = targetState.message.subject ?: "",
									modifier = Modifier
										.sharedElement(
											sharedContentState = rememberSharedContentState(key = targetState.message.id),
											animatedVisibilityScope = this@AnimatedContent,
										),
									sender = sender?.name,
									imageUrl = sender?.avatarUrl,
									time = targetState.message.dateTime,
									unread = targetState.message.unread
								)

								// If loading failed: error message instead of content
								MessageBubble(
									modifier = Modifier
										.fillMaxWidth()
										.padding(horizontal = 16.dp, vertical = 8.dp),
									icon = {
										Icon(
											painter = painterResource(id = com.sapuseven.untis.core.ui.R.drawable.core_ui_error),
											contentDescription = stringResource(id = com.sapuseven.untis.core.ui.R.string.all_error)
										)
									},
									colors = MessageBubbleDefaults.errorColors(),
									messageText = R.string.feature_infocenter_messages_details_error.takeIf { targetState is MessageDetailsState.Error },
									messageTextRaw = (targetState as? MessageDetailsState.Error)?.error
								)

								// Content with loading placeholder
								AnimatedVisibility(targetState !is MessageDetailsState.Error) {
									Column {
										// Body text
										Text(
											modifier = Modifier
												.fillMaxWidth()
												.padding(horizontal = 16.dp)
												.verticalScroll(rememberScrollState())
												.placeholder(
													visible = targetState is MessageDetailsState.Loading,
													color = PlaceholderDefaults.color(),
													shape = RoundedCornerShape(4.dp),
													highlight = PlaceholderHighlight.shimmer(
														PlaceholderDefaults.shimmerHighlightColor()
													)
												),
											style = MaterialTheme.typography.bodyMedium,
											text = AnnotatedString.fromHtml((targetState as? MessageDetailsState.Success)?.fullContent ?: "")
										)
										Spacer(modifier = Modifier.height(12.dp))

										// Attachments
										LazyColumn(
											modifier = Modifier.fillMaxWidth()
										) {
											items(messageState?.message?.attachments ?: emptyList()) {
												ListItem(
													colors = ListItemDefaults.colors(containerColor = Color.Transparent),
													headlineContent = { Text(
														style = MaterialTheme.typography.labelLargeEmphasized,
														color = MaterialTheme.colorScheme.onSecondaryContainer,
														text = it.name
													) },
													leadingContent = {
														Icon(
															painter = painterResource(id = com.sapuseven.untis.core.ui.R.drawable.core_ui_document),
															tint = MaterialTheme.colorScheme.onSecondaryContainer,
															contentDescription = null
														)
													},
													modifier = Modifier
														.clickable {
															uriHandler.openUri(it.url)
														}
												)
											}
										}
									}
								}

								if (canReply || canDelete) {
									Row(
										modifier = Modifier
											.fillMaxWidth()
											.padding(horizontal = 16.dp, vertical = 12.dp),
										horizontalArrangement = Arrangement.spacedBy(12.dp)
									) {
										if (canReply) {
											FilledTonalButton(
												modifier = Modifier.weight(1f),
												onClick = { onReply() }
											) {
												Text(text = "Reply")
											}
										}
										if (canDelete) {
											var showDeleteConfirmation by remember { mutableStateOf(false) }

											FilledTonalButton(
												modifier = Modifier.weight(1f),
												colors = if (showDeleteConfirmation)
													ButtonDefaults.filledTonalButtonColors(
														containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
														contentColor = MaterialTheme.colorScheme.onError,
													)
												else
													ButtonDefaults.filledTonalButtonColors(),
												onClick = {
													if (showDeleteConfirmation)
														onDelete()
													else
														showDeleteConfirmation = true
												}
											) {
												Text(text = if (showDeleteConfirmation) "Confirm" else "Delete")
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
	}
}

@Preview
@Composable
private fun MessageDetailsCardDirectMessageWithAttachmentPreview() {
	SharedTransitionLayout {
		MessageDetailsCard(
			messageState = MessageDetailsState.Success(
				message = Message.Direct(DirectMessage(
					id = 1L,
					timestamp = LocalDateTime(2024, 6, 1, 12, 0),
					subject = "Subject: Direct message with attachment",
					body = "This is the preview content of the message.",
					attachments = listOf(
						Attachment(
							id = 1L,
							name = "Attachment.pdf",
							url = "https://example.com/homework.pdf"
						),
					),
					unread = true,
					sender = MessageParticipant.User(
						id = 1L,
						name = "Message Sender",
						avatarUrl = null
					),
					recipients = listOf(
						MessageParticipant.User(
							id = 2L,
							name = "Message Recipient",
							avatarUrl = null
						),
					),
				)),
				fullContent = "This is the full content of the message.<br>It can be quite long and include multiple paragraphs, attachments, and other information.",
			),
			canReply = false,
			canDelete = false,
			onReply = {},
			onDelete = {}
		)
	}
}
