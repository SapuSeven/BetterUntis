package com.sapuseven.untis.adapters.infocenter

import com.sapuseven.untis.models.UntisExam
import com.sapuseven.untis.models.UntisHomework
import com.sapuseven.untis.models.untis.response.UntisHomeworkLesson

class EventAdapterItem(val exam: UntisExam?, val homework: UntisHomework?, val lessonsById: Map<String, UntisHomeworkLesson>?)
