package com.sapuseven.untis.api.model.untis

import kotlinx.serialization.Serializable

@Serializable
data class SchoolInfo(
	var server: String,
	var useMobileServiceUrlAndroid: Boolean,
	var useMobileServiceUrlIos: Boolean,
	var address: String,
	var displayName: String,
	var loginName: String,
	var schoolId: Long,
	var serverUrl: String,
	var mobileServiceUrl: String?
)
