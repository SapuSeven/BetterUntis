import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation("com.google.errorprone:error_prone_annotations:2.15.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    compileKotlin {
        compilerOptions.jvmTarget = JvmTarget.JVM_17
        compilerOptions.freeCompilerArgs = listOf(
            "-opt-in=kotlin.contracts.ExperimentalContracts",
            "-Xjvm-default=all-compatibility",
        )
    }
    compileTestKotlin {
        compilerOptions.jvmTarget = JvmTarget.JVM_17
    }
}
