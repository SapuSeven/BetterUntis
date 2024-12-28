import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
	alias(libs.plugins.agp.library)
	alias(libs.plugins.kotlin.android)
}

val composeCompilerVersion: String by project

android {
    compileSdk = 34
	namespace = "com.sapuseven.untis.views.weekview"

    defaultConfig {
        minSdk = 21
        targetSdk = 34
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = composeCompilerVersion
    }

	compileOptions {
		isCoreLibraryDesugaringEnabled = true

		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	kotlinOptions {
		jvmTarget = JavaVersion.VERSION_17.toString()
	}
}

dependencies {
    implementation(libs.joda.time)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

}
