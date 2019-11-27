package com.sapuseven.untis.helpers.config

object PreferenceUtils {
	fun getPrefInt(manager: PreferenceManager, key: String, convertString: Boolean = false): Int {
		val res = manager.context.resources

		return if (convertString)
			Integer.parseInt(manager.defaultPrefs.getString(key, res.getInteger(res.getIdentifier(key + "_default", "integer", manager.context.packageName)).toString())
					?: "")
		else
			manager.defaultPrefs.getInt(key, res.getInteger(res.getIdentifier(key + "_default", "integer", manager.context.packageName)))
	}

	fun getPrefBool(manager: PreferenceManager, key: String): Boolean {
		val res = manager.context.resources

		return manager.defaultPrefs.getBoolean(key, res.getBoolean(res.getIdentifier(key + "_default", "bool", manager.context.packageName)))
	}

	fun getPrefString(manager: PreferenceManager, key: String): String {
		val res = manager.context.resources

		return getPrefString(manager, key, res.getString(res.getIdentifier(key + "_default", "string", manager.context.packageName)))
				?: ""
	}

	fun getPrefString(manager: PreferenceManager, key: String, default: String?): String? {
		return manager.defaultPrefs.getString(key, default)
	}
}
