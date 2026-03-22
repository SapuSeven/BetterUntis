package com.sapuseven.untis.feature.infocenter.pages.messages

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.sapuseven.untis.core.model.timetable.Attachment
import com.sapuseven.untis.feature.infocenter.R
import io.github.fornewid.placeholder.foundation.PlaceholderDefaults
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.foundation.placeholder
import io.github.fornewid.placeholder.foundation.shimmer
import io.github.fornewid.placeholder.material3.color
import io.github.fornewid.placeholder.material3.shimmerHighlightColor
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.todayIn
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MessagePreview(
	subject: String,
	modifier: Modifier = Modifier,
	body: String? = null,
	sender: String? = null,
	imageUrl: String? = null,
	time: LocalDateTime? = null,
	unread: Boolean = false,
	attachments: List<Attachment>? = null,
	onShowAttachments: () -> Unit = {}
) {
	val textContent = @Composable {
		Column {
			subject.takeIf { it.isNotBlank() }?.let {
				Text(
					text = subject,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					fontWeight = if (unread) FontWeight.Bold else FontWeight.Normal,
					style = MaterialTheme.typography.titleMediumEmphasized,
					color = MaterialTheme.colorScheme.onSurface
				)
			}
			body?.let {
				Text(
					text = AnnotatedString.fromHtml(body),
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					fontWeight = if (unread) FontWeight.Bold else FontWeight.Normal,
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
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
					style = MaterialTheme.typography.titleSmall,
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
					text = if (time.date == Clock.System.todayIn(TimeZone.currentSystemDefault())) // TODO use injected clock/timezone
						time.toJavaLocalDateTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
					else
						time.toJavaLocalDateTime().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			attachments?.takeIf { it.isNotEmpty() }?.let {
				IconButton(onClick = {
					onShowAttachments()
				}) {
					Icon(
						painter = painterResource(id = R.drawable.feature_infocenter_attachments),
						contentDescription = stringResource(id = R.string.feature_infocenter_messages_attachments)
					)
				}
			}
		}
	}
}
