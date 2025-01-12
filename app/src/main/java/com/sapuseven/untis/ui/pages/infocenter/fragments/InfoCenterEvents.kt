package com.sapuseven.untis.ui.pages.infocenter.fragments

import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.untis.classreg.Exam
import com.sapuseven.untis.api.model.untis.classreg.HomeWork
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.data.repository.LocalElementRepository
import com.sapuseven.untis.models.untis.response.UntisHomeworkLesson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


sealed class EventList {
	data class ExamItem(
		val exam: Exam
	) : EventList()

	data class HomeworkItem(
		val homework: HomeWork,
		val lessonsById: Map<String, UntisHomeworkLesson>
	) : EventList()
}

@Composable
fun InfoCenterEvents(events: List<EventList>?) {
	ItemList(
		itemResult = events,
		itemRenderer = { EventItem(it) },
		itemsEmptyMessage = R.string.infocenter_events_empty
	)
}

@Composable
private fun EventItem(item: EventList) {
	when (item) {
		is EventList.ExamItem -> {
			val subject = LocalElementRepository.current.getShortName(
				item.exam.subjectId,
				ElementType.SUBJECT
			)
			ListItem(
				overlineContent = {
					Text(
						formatExamTime(
							item.exam.startDateTime,
							item.exam.endDateTime
						)
					)
				},
				headlineContent = {
					Text(
						if (!item.exam.name.contains(subject)) stringResource(
							R.string.infocenter_events_exam_name_long,
							subject,
							item.exam.name
						) else item.exam.name
					)
				}
			)
		}

		is EventList.HomeworkItem -> {
			ListItem(
				overlineContent = {
					Text(
						item.homework.endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
					)
				},
				headlineContent = {
					Text(
						LocalElementRepository.current.getLongName(
							item.lessonsById.get(item.homework.lessonId.toString())?.subjectId ?: 0,
							ElementType.SUBJECT
						)
					)
				},
				supportingContent = if (item.homework.text.isNotBlank()) {
					{ Text(item.homework.text) }
				} else null
			)
		}
	}
}

@Composable
private fun formatExamTime(startDateTime: LocalDateTime, endDateTime: LocalDateTime): String {
	return stringResource(
		if (startDateTime.dayOfYear == endDateTime.dayOfYear)
			R.string.infocenter_timeformat_sameday
		else
			R.string.infocenter_timeformat,
		startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE),
		startDateTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
		endDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE),
		endDateTime.format(DateTimeFormatter.ISO_LOCAL_TIME)
	)
}
