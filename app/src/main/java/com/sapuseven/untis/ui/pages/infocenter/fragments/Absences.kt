package com.sapuseven.untis.ui.pages.infocenter.fragments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.absence.StudentAbsence
import com.sapuseven.untis.data.database.entities.ExcuseStatusEntity
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun InfoCenterAbsences(absences: Result<List<StudentAbsence>>?, excuseStatuses: List<ExcuseStatusEntity>) {
	LazyColumn(
		horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()
	) {
		itemList(
			itemResult = absences,
			itemRenderer = { AbsenceItem(it, excuseStatuses) },
			itemsEmptyMessage = R.string.infocenter_absences_empty,
		)
	}
}

@Composable
private fun AbsenceItem(item: StudentAbsence, excuseStatuses: List<ExcuseStatusEntity>) {
	ListItem(
		headlineContent = {
			Text(item.startDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)))
		},
		supportingContent = {
			Column {
				// Time range
				Text(
					stringResource(
						R.string.infocenter_absences_timerange,
						item.startDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
						item.endDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
					)
				)

				// Reason
				Text(
					stringResource(
						R.string.infocenter_absence_reason,
						if (item.absenceReason.isNotEmpty())
							item.absenceReason.replaceFirstChar { it.uppercase() }
						else
							stringResource(R.string.infocenter_absence_reason_unknown)
					)
				)

				// Additional Text
				if (item.text.isNotBlank()) {
					Text(item.text)
				}

				// Excuse status
				item.excuse?.let { excuse -> excuseStatuses.find { it.id == excuse.excuseStatusId } }?.let {
					Text(
						stringResource(
							if (it.excused) R.string.infocenter_absence_excused_status
							else R.string.infocenter_absence_unexcused_status,
							it.longName
						)
					)
				} ?: Text(
					if (item.excused)
						stringResource(R.string.infocenter_absence_excused)
					else
						stringResource(R.string.infocenter_absence_unexcused)
				)
			}
		}
	)
}
