package com.sapuseven.untis.ui.datepicker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.joda.time.LocalDate

@Composable
fun DatePicker(
	modifier: Modifier = Modifier,
	selectedDayState: MutableState<LocalDate>
) {
	var displayedDate by remember { mutableStateOf(selectedDayState.value) }

	Column(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp)
	) {
		DatePickerHeader(
			date = displayedDate,
			onPreviousClick = {
				displayedDate = displayedDate.minusMonths(1)
			},
			onNextClick = {
				displayedDate = displayedDate.plusMonths(1)
			}
		)

		DatePickerGrid(
			date = displayedDate,
			selectedDay = selectedDayState.value
		) {
			selectedDayState.value = it
		}
	}
}
