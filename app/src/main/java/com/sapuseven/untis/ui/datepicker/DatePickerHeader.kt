package com.sapuseven.untis.ui.datepicker

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DatePickerHeader(
	modifier: Modifier = Modifier,
	date: LocalDate,
	onPreviousClick: () -> Unit = {},
	onNextClick: () -> Unit = {},
) {
	val isNext = remember { mutableStateOf(true) }
	Row(
		modifier = modifier
			.padding(start = 8.dp, top = 8.dp)
			.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		AnimatedContent(
			targetState = date,
			transitionSpec = {
				if (targetState > initialState) {
					slideInVertically { height -> height } + fadeIn() with slideOutVertically { height -> -height } + fadeOut()
				} else {
					slideInVertically { height -> -height } + fadeIn() with slideOutVertically { height -> height } + fadeOut()
				}.using(SizeTransform(clip = false))
			}
		) {
			Text(
				text = getTitleText(it),
				color = MaterialTheme.colorScheme.onSurface,
				style = MaterialTheme.typography.titleMedium
			)
		}

		Row(
			horizontalArrangement = Arrangement.End,
		) {
			IconButton(
				onClick = {
					isNext.value = false
					onPreviousClick()
				},
				modifier = modifier.clip(CircleShape)
			) {
				Icon(
					imageVector = Icons.Outlined.KeyboardArrowLeft,
					contentDescription = stringResource(R.string.all_datepicker_month_previous)
				)
			}

			IconButton(
				onClick = {
					isNext.value = false
					onNextClick()
				},
				modifier = modifier.clip(CircleShape)
			) {
				Icon(
					imageVector = Icons.Outlined.KeyboardArrowRight,
					contentDescription = stringResource(R.string.all_datepicker_month_next)
				)
			}
		}
	}
}

internal fun getTitleText(date: LocalDate): String = date.toString(DateTimeFormat.forPattern("MMMM y"))
