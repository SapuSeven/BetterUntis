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
include(":weekview")
include(":material-color-utils")
include(":api")
include(":protostore")
