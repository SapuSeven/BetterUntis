import java.io.FileInputStream
import java.util.Date
import java.util.Properties

plugins {
	alias(libs.plugins.betteruntis.android.application)
	alias(libs.plugins.betteruntis.android.application.compose)
	alias(libs.plugins.betteruntis.android.application.sentry)
	alias(libs.plugins.betteruntis.android.flavors)
	alias(libs.plugins.betteruntis.hilt)

	alias(libs.plugins.mikepenz.aboutlibraries)
	alias(libs.plugins.mannodermaus.android.junit5)
	alias(libs.plugins.kotlin.parcelize)
}

// Auto-generates a new version code every minute
fun generateVersionCode(): Int {
	return (Date().time / 1000 / 60).toInt()
}

android {
	namespace = "com.sapuseven.untis"

	defaultConfig {
		applicationId = "com.sapuseven.untis"
		versionCode = generateVersionCode()
		versionName = "5.0.0-beta04"

		testInstrumentationRunner = "com.sapuseven.untis.HiltTestRunner"
	}

	signingConfigs {
		val propertiesFile = file("signing.properties")

		val signingProperties = Properties()
		if (propertiesFile.exists()) {
			signingProperties.load(FileInputStream(propertiesFile))
		}

		if (file("BetterUntis.jks").exists()) {
			create("release") {
				storeFile = file("BetterUntis.jks")
				storePassword = signingProperties["keystorePassword"] as String? ?: System.getenv("KEYSTORE_PASSWORD")
				keyAlias = "release"
				keyPassword = signingProperties["keyReleasePassword"] as String? ?: System.getenv("KEY_RELEASE_PASSWORD")
			}

			getByName("debug") {
				storeFile = file("BetterUntis.jks")
				storePassword = signingProperties["keystorePassword"] as String? ?: System.getenv("KEYSTORE_PASSWORD")
				keyAlias = "debug"
				keyPassword = signingProperties["keyDebugPassword"] as String? ?: System.getenv("KEY_DEBUG_PASSWORD")
			}
		}
	}

	buildTypes {
		debug {
			applicationIdSuffix = ".debug"
			versionNameSuffix = "-DEBUG"

			if (file("BetterUntis.jks").exists()) {
				signingConfig = signingConfigs.getByName("debug")
			}
		}

		release {
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

			if (file("BetterUntis.jks").exists()) {
				signingConfig = signingConfigs.getByName("release")
			}
		}
	}

	packaging {
		resources.excludes.add("META-INF/gradle/incremental.annotation.processors")
	}

	lint {
		disable += "MissingTranslation"
	}
}

aboutLibraries {
	collect.includePlatform = false
	library.duplicationMode = com.mikepenz.aboutlibraries.plugin.DuplicateMode.MERGE
}

dependencies {
	//implementation(projects.feature.glance)
	implementation(projects.feature.automute)
	implementation(projects.feature.infocenter)
	implementation(projects.feature.login)
	implementation(projects.feature.notifications)
	implementation(projects.feature.roomfinder)
	implementation(projects.feature.settings)
	implementation(projects.feature.timetable)

	implementation(projects.core.api)
	implementation(projects.core.data)
	implementation(projects.core.database)
	implementation(projects.core.datastore)
	implementation(projects.core.domain)
	implementation(projects.core.ui)

	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.core.splashscreen)
	implementation(libs.androidx.hilt.navigation.compose)
	implementation(libs.androidx.hilt.work)
	implementation(libs.androidx.lifecycle)
	implementation(libs.androidx.work)
	implementation(libs.material)
	implementation(libs.coil)
	implementation(libs.kotlinx.serialization.json)

	ksp(libs.hilt.compiler)
	ksp(libs.androidx.hilt.compiler)

	kspTest(libs.hilt.compiler)

	// TODO: Move to feature module
	implementation(libs.mikepenz.aboutlibraries.core)
	implementation(libs.mikepenz.aboutlibraries.compose)

	// TODO: Move to relevant modules
	implementation(libs.ktor.client.cio)
	implementation(libs.ktor.client.content.negotiation)
	implementation(libs.ktor.serialization)

	// TODO: Move to relevant modules
	implementation(libs.andrew0000.cache)

	//implementation(libs.accompanist.swiperefresh)
	//implementation(libs.accompanist.systemuicontroller)
	//implementation(libs.accompanist.permissions)
	//implementation(libs.androidx.appcompat)
	//implementation(platform(libs.androidx.compose.bom))
	//implementation(libs.androidx.compose.animation)
	//implementation(libs.androidx.compose.runtime.livedata)
	//implementation(libs.androidx.compose.ui)
	//implementation(libs.androidx.compose.ui.tooling)
	//implementation(libs.androidx.constraintlayout)
	//implementation(libs.androidx.datastore)
	//implementation(libs.androidx.fragment)
	//implementation(libs.androidx.lifecycle.viewModelCompose)
	//implementation(libs.androidx.navigation.compose)
	//implementation(libs.androidx.preference)
	//implementation(libs.androidx.recyclerview)
	//implementation(libs.androidx.swiperefreshlayout)
	//implementation(libs.colormath)
	//implementation(libs.fuel)
	//implementation(libs.fuel.coroutines)
	//implementation(libs.fuel.serialization)
	//implementation(libs.hilt.android)
	//implementation(libs.hilt.compiler)
	//implementation(libs.kotlin.reflect)
	//implementation(libs.kotlinx.coroutines)
	//implementation(libs.kotlinx.serialization.cbor)
	//implementation(libs.material)
	//implementation(libs.material.theme.adapter)
	//implementation(libs.androidx.transition.ktx)
	//implementation(libs.fornewid.placeholder.material3)
	//implementation(libs.fornewid.material.motion.compose.core)

	//coreLibraryDesugaring(libs.android.desugarJdkLibs)

	//ksp(libs.androidx.hilt.compiler)
	//ksp(libs.dagger.compiler)

	//testImplementation(libs.junit.jupiter.api)
	//testImplementation(libs.junit.jupiter.params)
	//testRuntimeOnly(libs.junit.jupiter.engine)
	//testRuntimeOnly(libs.junit.vintage.engine)

	//testImplementation(libs.junit)
	//testImplementation(libs.mock)
	//testImplementation(libs.mockito)
	//testImplementation(libs.hamcrest)
	//testImplementation(libs.hamcrest.library)

	//androidTestImplementation(libs.androidx.test)
	//androidTestImplementation(libs.androidx.test.runner)
	//androidTestImplementation(libs.androidx.compose.ui.test)
	//androidTestImplementation(libs.hilt.android.testing)
	//debugImplementation(libs.androidx.compose.ui.test.manifest)
}
