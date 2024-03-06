package com.sapuseven.untis.ui.activities

import android.net.Uri
import android.os.Bundle

sealed class LoginDataInputEvents {
	data class DisplaySnackbar(
		val textRes: Int? = null
	) : LoginDataInputEvents()
}
