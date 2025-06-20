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
	var tenantId: String? = null, // Note: This value seems to sometimes be present in the response, but missing in the Untis Mobile sources. Observed: same value as `schoolId`
	var serverUrl: String,
	var mobileServiceUrl: String?
)
