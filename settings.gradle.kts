rootProject.name = "BetterUntis"

pluginManagement {
	repositories {
		gradlePluginPortal()
		google()
		mavenCentral()
	}
}

dependencyResolutionManagement {
	repositories {
		google()
		mavenCentral()
	}
}


include(":app")
include(":material-color-utils")
include(":api")
