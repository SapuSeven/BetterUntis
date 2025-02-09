import com.google.protobuf.gradle.id
import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.Date
import java.util.Properties

plugins {
	alias(libs.plugins.agp.application)
	alias(libs.plugins.sentry.gradle)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.mikepenz.aboutlibraries)
	alias(libs.plugins.ksp)
	alias(libs.plugins.mannodermaus.android.junit5)
	alias(libs.plugins.dagger.hilt)
	alias(libs.plugins.kotlin.parcelize)
	alias(libs.plugins.protobuf)
}

// Auto-generates a new version code every minute
fun generateVersionCode(): Int {
	return (Date().time / 1000 / 60).toInt()
}

val gmsImplementation: Configuration by configurations.creating

android {
	compileSdk = 35
	namespace = "com.sapuseven.untis"

	androidResources {
		generateLocaleConfig = true
	}

	defaultConfig {
		applicationId = "com.sapuseven.untis"
		minSdk = 21
		targetSdk = 35
		versionCode = generateVersionCode()
		versionName = "5.0.0-beta01"
		vectorDrawables.useSupportLibrary = true
		testInstrumentationRunner = "com.sapuseven.untis.HiltTestRunner"

		buildConfigField("String", "SENTRY_DSN", "\"https://d3b77222abce4fcfa74fda2185e0f8dc@o1136770.ingest.sentry.io/6188900\"")

		ksp {
			arg("room.schemaLocation", "$projectDir/schemas")
		}
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
		release {
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

			if (file("BetterUntis.jks").exists()) {
				signingConfig = signingConfigs.getByName("release")
			}
		}

		debug {
			isMinifyEnabled = false
			isShrinkResources = false
			applicationIdSuffix = ".debug"
			versionNameSuffix = "-DEBUG"

			if (file("BetterUntis.jks").exists()) {
				signingConfig = signingConfigs.getByName("debug")
			}
		}
	}

	buildFeatures {
		buildConfig = true
		compose = true
	}

	compileOptions {
		isCoreLibraryDesugaringEnabled = true

		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	kotlinOptions {
		jvmTarget = JavaVersion.VERSION_17.toString()
	}

	packaging {
		resources.excludes.add("META-INF/gradle/incremental.annotation.processors")
	}

	lint {
		disable += "MissingTranslation"
	}

	flavorDimensions += "dependencies"
	productFlavors {
		create("gms") {
			isDefault = true
			dimension = "dependencies"
		}

		create("foss") {
			dimension = "dependencies"
		}
	}
}

sentry {
	autoUploadProguardMapping.set(System.getenv("SENTRY_PROJECT") != null)
}

aboutLibraries {
	includePlatform = false
	duplicationMode = com.mikepenz.aboutlibraries.plugin.DuplicateMode.MERGE
}

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc:4.28.0"
	}

	// Generates the java Protobuf-lite code for the Protobufs in this project. See
	// https://github.com/google/protobuf-gradle-plugin#customizing-protobuf-compilation
	// for more information.
	generateProtoTasks {
		all().configureEach {
			builtins {
				id("java") {
					option("lite")
				}
			}
		}
	}

	androidComponents {
		onVariants(selector().all()) { variant ->
			afterEvaluate {
				val capName = variant.name.capitalized()
				tasks.getByName<KotlinCompile>("ksp${capName}Kotlin") {
					setSource(tasks.getByName("generate${capName}Proto").outputs)
				}
			}
		}
	}
}

dependencies {
	implementation(libs.accompanist.flowlayout)
	implementation(libs.accompanist.swiperefresh)
	implementation(libs.accompanist.systemuicontroller)
	implementation(libs.accompanist.permissions)
	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.compose.animation)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.compose.runtime.livedata)
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.tooling)
	implementation(libs.androidx.constraintlayout)
	implementation(libs.androidx.datastore)
	implementation(libs.androidx.fragment)
	implementation(libs.androidx.glance)
	implementation(libs.androidx.hilt.navigation.compose)
	implementation(libs.androidx.lifecycle)
	implementation(libs.androidx.lifecycle.compose)
	implementation(libs.androidx.navigation.compose)
	implementation(libs.androidx.preference)
	implementation(libs.androidx.recyclerview)
	implementation(libs.androidx.room.ktx)
	implementation(libs.androidx.room.runtime)
	implementation(libs.androidx.swiperefreshlayout)
	implementation(libs.androidx.work)
	implementation(libs.colormath)
	implementation(libs.coil)
	implementation(libs.fuel)
	implementation(libs.fuel.coroutines)
	implementation(libs.fuel.serialization)
	implementation(libs.joda.time)
	implementation(libs.hilt.android)
	implementation(libs.hilt.compiler)
	implementation(libs.kotlin.reflect)
	implementation(libs.kotlinx.coroutines)
	implementation(libs.kotlinx.serialization.cbor)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.ktor.client.cio)
	implementation(libs.ktor.client.content.negotiation)
	implementation(libs.ktor.serialization)
	implementation(libs.material)
	implementation(libs.material.theme.adapter)
	implementation(libs.mikepenz.aboutlibraries.compose)
	implementation(libs.mikepenz.aboutlibraries.core)
	implementation(libs.sentry.android)
	implementation(libs.sentry.compose.android)
	implementation(libs.zxing)
	implementation(libs.protobuf.javalite)
	implementation(libs.androidx.transition.ktx)
	implementation(libs.andrew0000.cache)
	implementation(libs.sapuseven.protostore)
	implementation(libs.fornewid.placeholder.material3)
	implementation(libs.fornewid.material.motion.compose.core)

	gmsImplementation(libs.gms.code.scanner)

	coreLibraryDesugaring(libs.desugar.jdk.libs)

	ksp(libs.androidx.room.compiler)
	ksp(libs.dagger.compiler)
	ksp(libs.hilt.compiler)
	kspTest(libs.hilt.compiler)

	testImplementation(libs.junit.jupiter.api)
	testImplementation(libs.junit.jupiter.params)
	testRuntimeOnly(libs.junit.jupiter.engine)
	testRuntimeOnly(libs.junit.vintage.engine)

	testImplementation(libs.junit)
	testImplementation(libs.mock)
	testImplementation(libs.mockito)
	testImplementation(libs.hamcrest)
	testImplementation(libs.hamcrest.library)

	androidTestImplementation(libs.androidx.test)
	androidTestImplementation(libs.androidx.test.runner)
	androidTestImplementation(libs.androidx.compose.ui.test)
	androidTestImplementation(libs.hilt.android.testing)
	debugImplementation(libs.androidx.compose.ui.test.manifest)

	implementation(project(":api"))
	implementation(project(":material-color-utils"))
}
