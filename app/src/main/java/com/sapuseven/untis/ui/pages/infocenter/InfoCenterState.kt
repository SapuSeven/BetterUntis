package com.sapuseven.untis.ui.pages.infocenter

	/*private var api: UntisRequest = UntisRequest()

		val absenceList: List<UntisAbsence>?
			get() = absences.value.let {
				if (absencesSortReversed.value) {
					it?.sortedBy { absence -> absence.startDateTime.toLocalDateTime() } // oldest first
				} else {
					it?.sortedByDescending { absence -> absence.startDateTime.toLocalDateTime() } // newest first
				}
			}.let {
				it?.filter { absence ->
					(absencesOnlyUnexcused.value != absence.excused) || !absence.excused
				}
			}.let {
				when (absencesTimeRange.value) {
					"seven_days" -> 7
					"fourteen_days" -> 14
					"thirty_days" -> 30
					"ninety_days" -> 90
					else -> null
				}?.let { days ->
					it?.filter { absence ->
						LocalDateTime.now().minusDays(days)
							.isBefore(absence.startDateTime.toLocalDateTime())
					}
				} ?: it
			}
*/
