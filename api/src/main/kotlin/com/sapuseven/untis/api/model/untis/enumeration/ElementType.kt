package com.sapuseven.untis.api.model.untis.enumeration

enum class ElementType(val id: Int) {
	CLASS(1),
	TEACHER(2),
	SUBJECT(3),
	ROOM(4),
	STUDENT(5),
	PARENT(15),

	@Deprecated("Not present in Untis API anymore")
	LEGAL_GUARDIAN(12),

	@Deprecated("Not present in Untis API anymore")
	APPRENTICE_REPRESENTATIVE(21)
}

