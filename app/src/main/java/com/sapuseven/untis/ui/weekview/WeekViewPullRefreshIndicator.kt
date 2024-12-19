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
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ProgressIndicatorDefaults.IndicatorBackgroundOpacity
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.ui.common.SmallCircularProgressIndicator

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun WeekViewPullRefreshIndicator(
	refreshing: Boolean,
	state: PullRefreshState,
	collapsing: Boolean = true,
	backgroundColor: Color = MaterialTheme.colorScheme.primary,
	contentColor: Color = MaterialTheme.colorScheme.onPrimary,//contentColorFor(backgroundColor),
	collapsedHeight: Int = 4,
	triggerHeight: Int = 48,
	modifier: Modifier = Modifier
) {
	val pullDistance by animateDpAsState(
		targetValue = when {
			refreshing -> if (collapsing) collapsedHeight.dp else triggerHeight.dp
			state.progress in 0f..1f -> (state.progress * triggerHeight).dp
			state.progress > 1f -> (triggerHeight + (((state.progress - 1f) * .2f) * triggerHeight)).dp
			else -> 0.dp
		}, label = "pullDistance"
	)
	val iconRotation by animateFloatAsState(
		targetValue = when {
			state.progress in 0f..1f -> (state.progress * 180f)
			state.progress > 1f -> (180f + (((state.progress - 1f) * .2f) * 180f))
			else -> 0f
		}, label = "iconRotation"
	)

	Box(
		modifier = modifier
            .height(pullDistance)
            .background(backgroundColor)
	) {
		Row(
            Modifier
                .align(Alignment.Center)
                .alpha(if (!refreshing || collapsing) ((state.progress - .2f).coerceIn(0f, 1f)) else 1f)
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
				backgroundColor = backgroundColor
					.copy(alpha = IndicatorBackgroundOpacity)
					.compositeOver(MaterialTheme.colorScheme.surface),
				modifier = Modifier
					.fillMaxSize()
			)
		}
	}
}
