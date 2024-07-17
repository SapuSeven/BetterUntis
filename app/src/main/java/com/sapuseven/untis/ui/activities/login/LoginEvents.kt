package com.sapuseven.untis.ui.activities.login

import android.net.Uri
import android.os.Bundle

sealed class LoginEvents {
	data object ClearFocus : LoginEvents()
}
