package com.sapuseven.untis.helpers

object TextUtils {
	fun isNullOrEmpty(obj: Any?): Boolean {
		return obj?.toString()?.length ?: 0 == 0
	}
}