package com.sapuseven.untis.ui.navigation

import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import com.sapuseven.untis.api.model.untis.SchoolInfo
import com.sapuseven.untis.helpers.SerializationUtils.getJSON
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object Routes {
	@Serializable
	object Splash

	@Serializable
	data class Timetable(val userId: Long)

	@Serializable
	object Login

	@Serializable
	data class LoginDataInput(
		var demoLogin: Boolean = false,
		var profileUpdate: Boolean = false,
		var schoolInfoSerialized: String? = null,
		var userId: Long = -1
	)

	@Serializable
	data class Settings(val userId: Long)
}
