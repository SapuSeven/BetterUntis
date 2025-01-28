package com.sapuseven.untis.ui.pages.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.sapuseven.untis.R
import com.sapuseven.untis.data.model.github.GitHubUser
import com.sapuseven.untis.ui.common.ifNotNull
import io.github.fornewid.placeholder.foundation.PlaceholderDefaults
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.foundation.placeholder
import io.github.fornewid.placeholder.foundation.shimmer
import io.github.fornewid.placeholder.material3.color
import io.github.fornewid.placeholder.material3.shimmerHighlightColor

@Composable
fun Contributor(
	githubUser: GitHubUser?,
	onClick: (() -> Unit)? = null
) {
	ListItem(
		modifier = Modifier.ifNotNull(onClick) { clickable(onClick = it) },
		headlineContent = {
			Text(
				text = githubUser?.login ?: "",
				modifier = Modifier
					.fillMaxWidth()
					.placeholder(
						visible = githubUser == null,
						color = PlaceholderDefaults.color(),
						shape = RoundedCornerShape(4.dp),
						highlight = PlaceholderHighlight.shimmer(PlaceholderDefaults.shimmerHighlightColor())
					)
			)
		},
		supportingContent = {
			val alpha by animateFloatAsState(if (githubUser == null) 0f else 1f, label = "")

			Text(
				text = githubUser?.contributions?.let {
					pluralStringResource(
						id = R.plurals.preferences_contributors_contributions,
						count = it,
						it
					)
				} ?: "",
				modifier = Modifier
					.alpha(alpha)
			)
		},
		leadingContent = {
			val painter = rememberAsyncImagePainter(githubUser?.avatarUrl)

			Image(
				painter = painter,
				contentDescription = null,
				modifier = Modifier
					.size(48.dp)
					.clip(shape = RoundedCornerShape(4.dp))
					.placeholder(
						visible = painter.state !is AsyncImagePainter.State.Success,
						color = PlaceholderDefaults.color(),
						highlight = PlaceholderHighlight.shimmer(PlaceholderDefaults.shimmerHighlightColor())
					)
			)
		}
	)
}
