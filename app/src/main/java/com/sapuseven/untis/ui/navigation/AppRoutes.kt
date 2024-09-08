package com.sapuseven.untis.ui.navigation

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
		data object Timetable

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
