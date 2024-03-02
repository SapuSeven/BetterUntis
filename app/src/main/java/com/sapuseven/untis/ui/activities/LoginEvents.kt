package com.sapuseven.untis.ui.activities

import android.net.Uri
import android.os.Bundle

sealed class LoginEvents {
	data object ClearFocus : LoginEvents()
	data class StartLoginActivity(
		val data: Uri? = null,
		val extras: Bundle? = null
	) : LoginEvents()
}
