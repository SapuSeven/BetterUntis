package com.sapuseven.untis.ui.pages.infocenter.fragments

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.timetable.OfficeHour
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun InfoCenterOfficeHours(officeHours: Result<List<OfficeHour>>?) {
	LazyColumn(
		horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()
	) {
		itemList(
			itemResult = officeHours,
			itemRenderer = { OfficeHourItem(it) },
			itemsEmptyMessage = R.string.infocenter_absences_empty,
		)
	}
}

@Composable
private fun OfficeHourItem(item: OfficeHour) {
	val body = listOf(
		item.displayNameRooms,
		item.phone,
		item.email
	).filter { it?.isNotEmpty() == true }.joinToString("\n")

	ListItem(
		overlineContent = {
			Text(
				formatOfficeHourTime(item.startDateTime, item.endDateTime)
			)
		},
		headlineContent = { Text(item.displayNameTeacher) },
		supportingContent = if (body.isNotBlank()) {
			{ Text(body) }
		} else null
	)
}

@Composable
private fun formatOfficeHourTime(
	startDateTime: LocalDateTime,
	endDateTime: LocalDateTime,
): String {
	return stringResource(
		if (startDateTime.dayOfYear == endDateTime.dayOfYear)
			R.string.infocenter_timeformat_sameday
		else
			R.string.infocenter_timeformat,
		startDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
		startDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
		endDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
		endDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
	)
}
