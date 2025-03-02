package com.sapuseven.untis.data.database.entities

abstract class ElementEntity {
	abstract val id: Long
	abstract val userId: Long
	abstract val name: String
	abstract val foreColor: String?
	abstract val backColor: String?
	abstract val active: Boolean
}
