plugins {
	alias(libs.plugins.agp.library)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.ksp)
}

android {
	namespace = "com.sapuseven.untis.persistence"
	compileSdk = 35

	defaultConfig {
		minSdk = 21

		ksp {
			arg("room.schemaLocation", "$projectDir/schemas")
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	kotlinOptions {
		jvmTarget = JavaVersion.VERSION_17.toString()
	}
}

dependencies {
	implementation(libs.androidx.room.ktx)
	implementation(libs.androidx.room.runtime)

	ksp(libs.androidx.room.compiler)

	// <editor-fold desc="Fix crash from missing `beginTransactionReadOnly()` method in Room due to sqlite version mismatch">
	// see https://issuetracker.google.com/issues/400483860#comment7
	implementation("androidx.sqlite:sqlite:2.5.1") {
		exclude(group = "io.sentry", module = "sentry-android-sqlite")
	}
	implementation("androidx.sqlite:sqlite-ktx:2.5.1") {
		exclude(group = "io.sentry", module = "sentry-android-sqlite")
	}
	configurations.configureEach {
		resolutionStrategy {
			force("androidx.sqlite:sqlite:2.5.1")
			force("androidx.sqlite:sqlite-ktx:2.5.1")
		}
	}
	// </editor-fold>
}