package com.sapuseven.untis.ui.pages.infocenter.fragments

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.classreg.Exam
import com.sapuseven.untis.api.model.untis.classreg.HomeWork
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.data.repository.LocalMasterDataRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@Composable
fun InfoCenterEvents(uiState: EventsUiState) {
	Crossfade(targetState = uiState, label = "InfoCenter Events Content") { state ->
		LazyColumn(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.fillMaxSize()
		) {
			item {
				Text(
					text = stringResource(R.string.infocenter_exams),
					style = MaterialTheme.typography.labelLarge,
					modifier = Modifier.padding(bottom = 8.dp)
				)
			}

			when (state) {
				EventsUiState.Loading -> item { InfoCenterLoading() }
				is EventsUiState.Success -> {
					state.exams.fold(
						onSuccess = {
							if (state.isExamsEmpty) item {
								Text(
									text = stringResource(R.string.infocenter_exams_empty),
									textAlign = TextAlign.Center,
									modifier = Modifier.fillMaxWidth()
								)
							} else {
								items(it) { item -> ExamItem(item) }
							}
						},
						onFailure = { item { InfoCenterError(it) } }
					)
				}
			}

			item {
				Text(
					text = stringResource(R.string.infocenter_homework),
					style = MaterialTheme.typography.labelLarge,
					modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
				)
			}

			when (state) {
				EventsUiState.Loading -> item { InfoCenterLoading() }
				is EventsUiState.Success -> {
					state.homework.fold(
						onSuccess = {
							if (state.isHomeworkEmpty) item {
								Text(
									text = stringResource(R.string.infocenter_homework_empty),
									textAlign = TextAlign.Center,
									modifier = Modifier.fillMaxWidth()
								)
							}
							else {
								items(it) { item -> HomeworkItem(item) }
							}
						},
						onFailure = { item { InfoCenterError(it) } }
					)
				}
			}
		}
	}
}

@Composable
private fun ExamItem(item: Exam) {
	val subject = LocalMasterDataRepository.current.getShortName(
		item.subjectId,
		ElementType.SUBJECT
	)
	ListItem(
		overlineContent = {
			Text(formatExamTime(item.startDateTime, item.endDateTime))
		},
		headlineContent = {
			Text(
				if (!item.name.contains(subject)) stringResource(
					R.string.infocenter_events_exam_name_long,
					subject,
					item.name
				)
				else item.name
			)
		}
	)
}

@Composable
private fun HomeworkItem(item: HomeWork) {
	ListItem(
		overlineContent = {
			Text(item.endDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
		},
		headlineContent = {
			/* TODO Text(
				LocalMasterDataRepository.current.getLongName(
					item.lessonsById[item.homework.lessonId.toString()]?.subjectId ?: 0,
					ElementType.SUBJECT
				)
			)*/
		},
		supportingContent = if (item.text.isNotBlank()) {
			{ Text(item.text) }
		} else null
	)
}

@Composable
private fun formatExamTime(startDateTime: LocalDateTime, endDateTime: LocalDateTime): String {
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

sealed interface EventsUiState {
	data object Loading : EventsUiState

	data class Success(
		val exams: Result<List<Exam>>,
		val homework: Result<List<HomeWork>>
	) : EventsUiState {
		constructor(exams: List<Exam>, homework: List<HomeWork>) : this(Result.success(exams), Result.success(homework))

		val isExamsEmpty: Boolean get() = exams.getOrDefault(emptyList()).isEmpty()
		val isHomeworkEmpty: Boolean get() = homework.getOrDefault(emptyList()).isEmpty()
	}
}
