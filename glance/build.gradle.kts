plugins {
	alias(libs.plugins.agp.library)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
}

android {
	namespace = "com.sapuseven.untis.glance"
	compileSdk = 35

	defaultConfig {
		minSdk = 21
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	buildFeatures {
		compose = true
		viewBinding = true
	}

	/*composeOptions {
		kotlinCompilerExtensionVersion = "1.5.15"
	}*/

	kotlinOptions {
		jvmTarget = JavaVersion.VERSION_17.toString()
	}
}

dependencies {
	implementation(libs.androidx.glance)
	implementation(libs.androidx.glance.material3)
}