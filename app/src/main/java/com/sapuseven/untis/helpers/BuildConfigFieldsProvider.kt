package com.sapuseven.untis.helpers

import com.sapuseven.untis.BuildConfig

data class BuildConfigFields(
	val isDebug: Boolean,
	val buildType: String,
	val versionCode: Int,
	val versionName: String,
)

interface BuildConfigFieldsProvider {
	fun get(): BuildConfigFields
}

class ApplicationBuildConfigFieldsProvider : BuildConfigFieldsProvider {
	override fun get(): BuildConfigFields = BuildConfigFields(
		isDebug = BuildConfig.DEBUG,
		buildType = BuildConfig.BUILD_TYPE,
		versionCode = BuildConfig.VERSION_CODE,
		versionName = BuildConfig.VERSION_NAME,
	)
}
