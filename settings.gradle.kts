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
		mavenLocal()
	}
}


include(":app")
include(":material-color-utils")
include(":api")
