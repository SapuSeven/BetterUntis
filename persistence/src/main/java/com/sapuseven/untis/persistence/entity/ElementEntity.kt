package com.sapuseven.untis.persistence.entity

import com.sapuseven.untis.api.model.untis.enumeration.ElementType

abstract class ElementEntity {
	abstract val id: Long
	abstract val userId: Long
	abstract val name: String
	abstract val foreColor: String?
	abstract val backColor: String?
	abstract val active: Boolean

	abstract fun getType(): ElementType
	abstract fun getShortName(default: String = "?"): String
	abstract fun getLongName(default: String = "?"): String
	abstract fun isAllowed(): Boolean
}
