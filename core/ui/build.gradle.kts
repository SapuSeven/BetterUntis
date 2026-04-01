plugins {
	alias(libs.plugins.betteruntis.android.library)
	alias(libs.plugins.betteruntis.android.library.compose)
	//alias(libs.plugins.betteruntis.hilt)
}

android {
	namespace = "com.sapuseven.untis.core.ui"
}

dependencies {
	implementation(projects.core.datastore)
	implementation(projects.core.model)
	implementation(projects.materialColorUtils)

	api(libs.androidx.compose.material3)
	api(libs.androidx.compose.material.icons)
	api(libs.material)

	implementation(libs.accompanist.flowlayout)
	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.compose.ui)
}
