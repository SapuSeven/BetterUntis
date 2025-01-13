package com.sapuseven.untis.ui.pages.infocenter.fragments

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
fun InfoCenterEvents(
	exams: Result<List<Exam>>?,
	homework: Result<List<HomeWork>>?
) {
	LazyColumn(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.fillMaxSize()
	) {
		item {
			Text(
				text = "Exams",
				style = MaterialTheme.typography.labelLarge,
				modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
			)
		}

		itemList(
			itemResult = exams,
			itemRenderer = { ExamItem(it) },
			itemsEmptyMessage = R.string.infocenter_exams_empty
		)

		item {
			Text(
				text = "Homework",
				style = MaterialTheme.typography.labelLarge,
				modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
			)
		}

		itemList(
			itemResult = homework,
			itemRenderer = { HomeworkItem(it) },
			itemsEmptyMessage = R.string.infocenter_homework_empty
		)
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
