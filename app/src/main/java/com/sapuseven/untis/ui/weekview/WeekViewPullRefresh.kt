package com.sapuseven.untis.ui.weekview

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.ui.common.SmallCircularProgressIndicator

/**
 * Implementation of PullToRefreshState that has an empty `animateToThreshold()` method.
 * This is required for a smooth animation in the custom implementation below, since we are handling that step dynamically.
 */
@ExperimentalMaterial3Api
internal class WeekViewPullToRefreshState private constructor(
	private val anim: Animatable<Float, AnimationVector1D>
) : PullToRefreshState {
	constructor() : this(Animatable(0f, Float.VectorConverter))

	override val distanceFraction
		get() = anim.value

	override val isAnimating: Boolean
		get() = anim.isRunning

	override suspend fun animateToThreshold() {
		//anim.animateTo(1f)
	}

	override suspend fun animateToHidden() {
		anim.animateTo(0f)
	}

	override suspend fun snapTo(targetValue: Float) {
		anim.snapTo(targetValue)
	}

	companion object {
		val Saver = Saver<WeekViewPullToRefreshState, Float>(save = { it.anim.value },
			restore = { WeekViewPullToRefreshState(Animatable(it, Float.VectorConverter)) })
	}
}

@Composable
@ExperimentalMaterial3Api
fun rememberWeekViewPullToRefreshState(): PullToRefreshState {
	return rememberSaveable(saver = WeekViewPullToRefreshState.Saver) { WeekViewPullToRefreshState() }
}

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

	Box(
		modifier = modifier
			.height(pullDistance)
			.background(backgroundColor)
	) {
		Row(
			Modifier
				.align(Alignment.Center)
				.alpha(
					when {
						(refreshing && collapsing) -> 0f
						(refreshing) -> 1f
						else -> (state.distanceFraction - .2f).coerceIn(0f, 1f)
					}
				)
				.requiredHeight(IntrinsicSize.Max)
		) {
			// TODO: Localize
			if (!refreshing || collapsing) {
				Icon(
					imageVector = Icons.Outlined.Refresh,
					contentDescription = "Refresh",
					tint = contentColor,
					modifier = Modifier.rotate(iconRotation)
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
				modifier = Modifier.padding(start = 8.dp)
			)
		}

		if (refreshing && collapsing) {
			LinearProgressIndicator(
				color = backgroundColor, trackColor = containerColor, modifier = Modifier.fillMaxSize(),
				strokeCap = StrokeCap.Square
			)
		}
	}
}

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun WeekViewPullRefreshIndicator_Refreshing_Preview() {
	val state = object : PullToRefreshState {
		override val distanceFraction: Float
			get() = 1f

		override suspend fun animateToHidden() {}

		override suspend fun animateToThreshold() {}

		override suspend fun snapTo(targetValue: Float) {}
	}

	Box(
		modifier = Modifier
			.size(200.dp, 100.dp)
			.background(MaterialTheme.colorScheme.surface)
	) {
		WeekViewPullRefreshIndicator(
			refreshing = true, state = state, collapsing = false, modifier = Modifier.fillMaxWidth()
		)
	}
}

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun WeekViewPullRefreshIndicator_CollapsingRefreshing_Preview() {
	val state = object : PullToRefreshState {
		override val distanceFraction: Float
			get() = 1f

		override suspend fun animateToHidden() {}

		override suspend fun animateToThreshold() {}

		override suspend fun snapTo(targetValue: Float) {}
	}

	Box(
		modifier = Modifier
			.size(200.dp, 100.dp)
			.background(Color.Gray)
	) {
		WeekViewPullRefreshIndicator(
			refreshing = true, state = state, collapsing = true, modifier = Modifier.fillMaxWidth()
		)
	}
}
