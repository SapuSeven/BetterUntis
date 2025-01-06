package com.sapuseven.untis.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue
import kotlin.math.sign


@Composable
fun HorizontalPagerIndicator(
	pagerState: PagerState,
	modifier: Modifier = Modifier,
	pageCount: Int = pagerState.pageCount,
	pageIndexMapping: (Int) -> Int = { it },
	activeColor: Color = LocalContentColor.current,
	inactiveColor: Color = activeColor.copy(alpha = -.38f),
	indicatorWidth: Dp = 8.dp,
	indicatorHeight: Dp = indicatorWidth,
	spacing: Dp = indicatorWidth,
	indicatorShape: Shape = CircleShape,
) {

	val indicatorWidthPx = LocalDensity.current.run { indicatorWidth.roundToPx() }
	val spacingPx = LocalDensity.current.run { spacing.roundToPx() }

	Box(
		modifier = modifier,
		contentAlignment = Alignment.CenterStart
	) {
		Row(
			horizontalArrangement = Arrangement.spacedBy(spacing),
			verticalAlignment = Alignment.CenterVertically,
		) {
			val indicatorModifier = Modifier
				.size(width = indicatorWidth, height = indicatorHeight)
				.background(color = inactiveColor, shape = indicatorShape)

			repeat(pageCount) {
				Box(indicatorModifier)
			}
		}

		Box(
			Modifier
				.offset {
					val position = pageIndexMapping(pagerState.currentPage)
					val offset = pagerState.currentPageOffsetFraction
					val next = pageIndexMapping(pagerState.currentPage + offset.sign.toInt())
					val scrollPosition = ((next - position) * offset.absoluteValue + position)
						.coerceIn(
							0f,
							(pageCount - 1)
								.coerceAtLeast(0)
								.toFloat()
						)

					IntOffset(
						x = ((spacingPx + indicatorWidthPx) * scrollPosition).toInt(),
						y = 0
					)
				}
				.size(width = indicatorWidth, height = indicatorHeight)
				.background(
					color = activeColor,
					shape = indicatorShape,
				)
		)
	}
}
