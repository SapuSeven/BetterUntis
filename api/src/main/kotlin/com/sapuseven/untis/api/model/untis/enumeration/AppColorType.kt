package com.sapuseven.untis.api.model.untis.enumeration

enum class AppColorType(val id: Int) {
	FREE(1),
	LESSON(2),
	RESERVATION(3),
	BOOK(4),
	STORNO(5),
	BK_LOCK(6),
	HOLIDAY(7),
	HOLIDAY_LOCK(8),
	CONFLICT(9),
	SUBST(10),
	CANCELLED(11),
	WITHOUT_ELEM(12),
	ELEM_CHANGED(13),
	SHIFT(14),
	SPECIALDUTY(15),
	EXAM(16),
	BREAKSUPERVISION(17),
	STANDBY(18),
	OFFICEHOUR(19),
	ABSENCE(20),
	WORKTIME(21);
}
