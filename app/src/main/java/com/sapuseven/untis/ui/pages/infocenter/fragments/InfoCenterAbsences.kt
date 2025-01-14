package com.sapuseven.untis.ui.pages.infocenter.fragments

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.absence.StudentAbsence
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun InfoCenterAbsences(absences: Result<List<StudentAbsence>>?) {
	LazyColumn(
		horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()
	) {
		itemList(
			itemResult = absences,
			itemRenderer = { AbsenceItem(it) },
			itemsEmptyMessage = R.string.infocenter_absences_empty,
		)
	}
}

@Composable
private fun AbsenceItem(item: StudentAbsence) {
	ListItem(overlineContent = {
		Text(
			formatAbsenceTime(
				item.startDateTime, item.endDateTime
			)
		)
	}, headlineContent = {
		Text(
			if (item.absenceReason.isNotEmpty()) item.absenceReason.substring(0, 1)
				.uppercase() + item.absenceReason.substring(1)
			else stringResource(R.string.infocenter_absence_unknown_reason)
		)
	}, supportingContent = if (item.text.isNotBlank()) {
		{ Text(item.text) }
	} else null, leadingContent = {
		if (item.excused) Icon(
			painter = painterResource(R.drawable.infocenter_absences_excused),
			contentDescription = stringResource(id = R.string.infocenter_absence_excused)
		)
		else Icon(
			painter = painterResource(R.drawable.infocenter_absences_unexcused),
			contentDescription = stringResource(id = R.string.infocenter_absence_unexcused)
		)
	})
}

@Composable
private fun formatAbsenceTime(
	startDateTime: LocalDateTime,
	endDateTime: LocalDateTime,
): String {
	return stringResource(
		if (startDateTime.dayOfYear == endDateTime.dayOfYear) R.string.infocenter_timeformat_sameday
		else R.string.infocenter_timeformat,
		startDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
		startDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
		endDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
		endDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
	)
}
