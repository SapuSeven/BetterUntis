package com.sapuseven.untis.models

import kotlinx.serialization.Serializable

@Serializable
data class UntisSchoolInfo(
	var server: String,
	var useMobileServiceUrlAndroid: Boolean,
	var useMobileServiceUrlIos: Boolean,
	var address: String,
	var displayName: String,
	var loginName: String,
	var schoolId: Int,
	var serverUrl: String,
	var mobileServiceUrl: String?
)
