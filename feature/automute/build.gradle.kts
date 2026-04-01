plugins {
	alias(libs.plugins.betteruntis.android.feature)
	alias(libs.plugins.betteruntis.android.library.compose)
}

android {
	namespace = "com.sapuseven.untis.feature.automute"
}

dependencies {
	implementation(projects.core.data)
	implementation(projects.core.domain)
	implementation(projects.core.ui)
	lintChecks(projects.core.lint)
}
