package com.sapuseven.untis.ui.navigation

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable

object AppRoutes {
	@Serializable
	data object Splash

	@Serializable
	data object Timetable

	@Serializable
	data object Login

	@Serializable
	data class LoginDataInput(
		val demoLogin: Boolean = false,
		val profileUpdate: Boolean = false,
		val schoolInfoSerialized: String? = null,
		val userId: Long = -1
	)

	@Serializable
	data object Settings {

		@Serializable
		data object Categories

		@Serializable
		data object General

		@Serializable
		data object Styling

		@Serializable
		data class Timetable(@StringRes val highlightTitle: Int)

		@Serializable
		data object Notifications

		@Serializable
		data object Connectivity

		@Serializable
		data object About {

			@Serializable
			data object Libraries

			@Serializable
			data object Contributors
		}
	}
}
