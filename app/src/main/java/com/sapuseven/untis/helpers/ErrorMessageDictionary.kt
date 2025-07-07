package com.sapuseven.untis.helpers

import com.sapuseven.untis.R
import com.sapuseven.untis.api.model.response.UntisErrorCode

object ErrorMessageDictionary {
	fun getErrorMessageResource(code: UntisErrorCode?, fallbackToGeneric: Boolean = true): Int? {
		return when (code) {
			UntisErrorCode.TOO_MANY_SCHOOL_SEARCH_RESULTS -> R.string.errormessagedictionary_too_many_results
			UntisErrorCode.INVALID_SCHOOL -> R.string.errormessagedictionary_invalid_school
			UntisErrorCode.INVALID_PASSWORD -> R.string.errormessagedictionary_invalid_credentials
			UntisErrorCode.NO_RIGHT -> R.string.errormessagedictionary_no_right
			UntisErrorCode.LOCKED_ACCESS -> R.string.errormessagedictionary_user_locked
			UntisErrorCode.NO_PUBLIC_ACCESS -> R.string.errormessagedictionary_no_public_access
			UntisErrorCode.INVALID_CLIENT_TIME -> R.string.errormessagedictionary_invalid_time_settings
			UntisErrorCode.REQUIRE2_FACTOR_AUTHENTICATION_TOKEN -> R.string.errormessagedictionary_second_factor_requried
			else -> if (fallbackToGeneric) R.string.errormessagedictionary_generic else null
		}
	}
}
