package com.sapuseven.untis.helpers.issues

import android.content.Context

abstract class Issue(val type: Type, val log: String) {
	enum class Type {
		CRASH,
		EXCEPTION,
		OTHER
	}

	abstract fun launch(context: Context)
}
