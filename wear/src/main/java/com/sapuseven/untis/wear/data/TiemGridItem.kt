package com.sapuseven.untis.wear.data

import com.sapuseven.untis.data.timetable.PeriodData
import org.joda.time.DateTime

class TimeGridItem(
        id: Long,
        val startDateTime: DateTime,
        var endDateTime: DateTime,
        val contextType: String,
        val periodData: PeriodData
) {

    init {
        periodData.setup()
    }
}
