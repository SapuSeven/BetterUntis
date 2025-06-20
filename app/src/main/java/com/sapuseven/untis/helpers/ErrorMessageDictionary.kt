package com.sapuseven.untis.helpers

import android.content.res.Resources
import com.sapuseven.untis.R

object ErrorMessageDictionary {
	/* Original app used the following error codes at the time of writing (may change over time):

		InvalidSchool(-8500),
		NoSpecifiedUser(-8502),
		InvalidPassword(-8504),
		NoRight(-8509),
		LockedAccess(-8511),
		RequiredAuthentication(-8520),
		AuthenticationError(-8521),
		NoPublicAccess(-8523),
		InvalidClientTime(-8524),
		InvalidUserStatus(-8525),
		InvalidUserRole(-8526),
		InvalidTimeTableType(-7001),
		InvalidElementId(-7002),
		InvalidPersonType(-7003),
		InvalidDate(-7004),
		UnspecifiedError(-8998);
		*/
	const val ERROR_CODE_TOO_MANY_RESULTS = -6003
	const val ERROR_CODE_INVALID_SCHOOLNAME = -8500
	const val ERROR_CODE_INVALID_CREDENTIALS = -8504
	const val ERROR_CODE_NO_RIGHT = -8509
	const val ERROR_CODE_USER_LOCKED = -8511
	const val ERROR_CODE_NO_PUBLIC_ACCESS_AVAILABLE = -8523
	const val ERROR_CODE_INVALID_CLIENT_TIME = -8524
	const val ERROR_CODE_NO_SERVER_FOUND = 100
	const val ERROR_CODE_WEBUNTIS_NOT_INSTALLED = 101

	fun getErrorMessage(resources: Resources, code: Int?, fallback: String? = null): String {
		return getErrorMessageResource(code, false)?.let { resources.getString(it) }
			?: fallback
			?: resources.getString(R.string.errormessagedictionary_generic)
	}

	fun getErrorMessageResource(code: Int?, fallbackToGeneric: Boolean = true): Int? {
		return when (code) {
			ERROR_CODE_TOO_MANY_RESULTS -> R.string.errormessagedictionary_too_many_results
			ERROR_CODE_INVALID_SCHOOLNAME -> R.string.errormessagedictionary_invalid_school
			ERROR_CODE_INVALID_CREDENTIALS -> R.string.errormessagedictionary_invalid_credentials
			ERROR_CODE_NO_RIGHT -> R.string.errormessagedictionary_no_right
			ERROR_CODE_USER_LOCKED -> R.string.errormessagedictionary_user_locked
			ERROR_CODE_NO_PUBLIC_ACCESS_AVAILABLE -> R.string.errormessagedictionary_no_public_access
			ERROR_CODE_INVALID_CLIENT_TIME -> R.string.errormessagedictionary_invalid_time_settings
			ERROR_CODE_NO_SERVER_FOUND -> R.string.errormessagedictionary_invalid_server_url
			ERROR_CODE_WEBUNTIS_NOT_INSTALLED -> R.string.errormessagedictionary_server_webuntis_not_installed
			else -> if (fallbackToGeneric) R.string.errormessagedictionary_generic else null
		}
	}
}
