package com.sapuseven.untis.ui.pages.infocenter.fragments

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.absence.StudentAbsence
import com.sapuseven.untis.persistence.entity.ExcuseStatusEntity
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun InfoCenterAbsences(uiState: AbsencesUiState) {
	Crossfade(targetState = uiState, label = "InfoCenter Absences Content") { state ->
		when (state) {
			AbsencesUiState.Loading -> InfoCenterLoading()
			is AbsencesUiState.Success -> {
				state.absences.fold(
					onSuccess = {
						if (state.isEmpty) {
							Text(
								text = stringResource(R.string.infocenter_absences_empty),
								textAlign = TextAlign.Center,
								modifier = Modifier.fillMaxWidth()
							)
						} else {
							LazyColumn(
								horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()
							) {
								items(it) {
									AbsenceItem(it, state.excuseStatuses)
								}
							}
						}
					},
					onFailure = { InfoCenterError(it) }
				)
			}
		}
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

sealed interface AbsencesUiState {
	data object Loading : AbsencesUiState

	data class Success(
		val absences: Result<List<StudentAbsence>>,
		val excuseStatuses: List<ExcuseStatusEntity>
	) : AbsencesUiState {

		val isEmpty: Boolean get() = absences.getOrDefault(emptyList()).isEmpty()
	}
}
