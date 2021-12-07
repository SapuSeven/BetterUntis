package com.sapuseven.untis.helpers.api

import androidx.annotation.StringRes

data class LoginErrorInfo(
		val errorCode: Int? = null,
		val errorMessage: String? = null,
		@StringRes val errorMessageStringRes: Int? = null
)
