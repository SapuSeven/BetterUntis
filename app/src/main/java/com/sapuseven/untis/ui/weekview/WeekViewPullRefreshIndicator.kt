package com.sapuseven.untis.ui.weekview

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.ui.common.SmallCircularProgressIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekViewPullRefreshIndicator(
	refreshing: Boolean,
	state: PullToRefreshState,
	collapsing: Boolean = true,
	backgroundColor: Color = MaterialTheme.colorScheme.primary,
	contentColor: Color = MaterialTheme.colorScheme.onPrimary,
	containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
	collapsedHeight: Int = 4,
	triggerHeight: Int = 32,
	modifier: Modifier = Modifier
) {
	val pullDistance by animateDpAsState(
		targetValue = when {
			refreshing -> if (collapsing) collapsedHeight.dp else triggerHeight.dp
			else -> (state.distanceFraction * triggerHeight).dp
		}, label = "pullDistance"
	)
	val iconRotation by animateFloatAsState(
		targetValue = when {
			state.distanceFraction in 0f..1f -> (state.distanceFraction * 180f)
			state.distanceFraction > 1f -> (180f + (((state.distanceFraction - 1f) * .2f) * 180f))
			else -> 0f
		}, label = "iconRotation"
	)

	/* The current height calculation has one flaw.
	 * On release, the PullToRefresh internal code is called in the following order:
	 *   animateToThreshold()
     *   onRefresh()
     * This means that the height is first animated to a distance fraction of 1.0f, then the refreshing state is set to `true`.
     * When we set the height in the pullDistance calculation above, the collapseHeight only takes effect when refreshing is `true`.
     * This causes the height first to animate to triggerHeight and then to collapsedHeight in different speeds, resulting in an uneven animation.
     * Since there is no way to know if the indicator is released before refreshing is set to true,
     * it seems to be impossible to work around this with the current implementation of PullToRefresh.
	 */
	Box(
		modifier = modifier
			.height(pullDistance)
			.background(backgroundColor)
	) {
		Row(
			Modifier
				.align(Alignment.Center)
				.alpha(if (!refreshing || collapsing) ((state.distanceFraction - .2f).coerceIn(0f, 1f)) else 1f)
				.requiredHeight(IntrinsicSize.Max)
		) {
			// TODO: Localize
			if (!refreshing || collapsing) {
				Icon(
					imageVector = Icons.Outlined.Refresh,
					contentDescription = "Refresh",
					tint = contentColor,
					modifier = Modifier
						.rotate(iconRotation)
				)
			} else {
				SmallCircularProgressIndicator(
					color = contentColor
				)
			}

			Text(
				text = "Refresh",
				color = contentColor,
				fontWeight = FontWeight.Bold,
				modifier = Modifier
					.padding(start = 8.dp)
			)
		}

		if (refreshing && collapsing) {
			LinearProgressIndicator(
				color = backgroundColor,
				trackColor = containerColor,
				modifier = Modifier
					.fillMaxSize()
			)
		}
	}
}
