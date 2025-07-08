import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.util.Locale

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.openapi.generator)
	alias(libs.plugins.kotlin.serialization)
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
//compileKotlin.dependsOn tasks.openApiGenerate

dependencies {
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.ktor.client.core)
	implementation(libs.ktor.client.content.negotiation)
	implementation(libs.ktor.serialization)

	testImplementation(libs.junit)
}

// Build Untis Internal OpenAPI specs
val apiSpecList = mutableListOf<File>()
val dir = file("${layout.projectDirectory}/spec/untis-intern/")
dir.walk().filter { it.isFile && Regex("""untis-.*\.yaml""").matches(it.name) }.forEach { file ->
	apiSpecList.add(file)
}
apiSpecList.forEach { file ->
	val apiName = file.name.replace(".yaml", "").replace("untis-", "")
	val taskName = "Untis" + apiName.split('-').joinToString("") { it.replaceFirstChar { c -> c.titlecase(Locale.getDefault()) } }
	val packageName = apiName.replace("-", "_")

	tasks.register("openApiGenerate$taskName", GenerateTask::class) {
		generatorName.set("kotlin")
		inputSpec.set("${layout.projectDirectory}/spec/untis-intern/untis-$apiName.yaml")
		outputDir.set("${layout.buildDirectory.get()}/generated")
		apiPackage.set("com.sapuseven.untis.api.$packageName")
		modelPackage.set("com.sapuseven.untis.model.$packageName")
		configOptions.set(
			mapOf(
				"library" to "jvm-ktor",
				"dateLibrary" to "java8",
				"serializationLibrary" to "kotlinx_serialization"
			)
		)
		typeMappings.set(
			mapOf(
				"java.time.OffsetDateTime" to "com.sapuseven.untis.api.serializer.DateTime"
			)
		)
	}
	tasks.named("compileKotlin").configure {
		dependsOn("openApiGenerate$taskName")
	}
}

kotlin.sourceSets.named("main") {
	kotlin.srcDir("${layout.buildDirectory.get()}/generated/src/main/kotlin")
}
