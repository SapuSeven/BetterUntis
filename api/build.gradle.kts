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
dir.walk().filter { it.isFile && Regex("""untis-.*-v\d\.yaml""").matches(it.name) }.forEach { file ->
	apiSpecList.add(file)
}
apiSpecList.forEach { file ->
	val fileName = file.name.replace(".yaml", "")
	val taskName = fileName.split('-').joinToString("") { it.replaceFirstChar { c -> c.titlecase(Locale.getDefault()) } }
	val packageName = fileName.replace("-", "_")

	tasks.register("openApiGenerate$taskName", GenerateTask::class) {
		generatorName.set("kotlin")
		inputSpec.set("${layout.projectDirectory}/spec/untis-intern/$fileName.yaml")
		outputDir.set("${layout.buildDirectory.get()}/generated")
		apiPackage.set("com.sapuseven.untis.api.$packageName")
		modelPackage.set("com.sapuseven.untis.model.$packageName")
		// templateDir.set("$rootDir/src/main/resources/api/templates")
		// https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/jaxrs-spec.md
		configOptions.set(
			mapOf(
				"library" to "jvm-ktor",
				"dateLibrary" to "java8",
				"serializationLibrary" to "kotlinx_serialization"
			)
		)
	}
	tasks.named("compileKotlin").configure {
		dependsOn("openApiGenerate$taskName")
	}
}

/*openApiGenerate {
	generatorName.set("kotlin")
	inputSpec.set("${layout.projectDirectory}/spec/untis.yaml")
	outputDir.set("$buildDir/generated")
	apiPackage.set("com.sapuseven.untis.api")
	modelPackage.set("com.sapuseven.untis.model")
	configOptions.set(
		mapOf(
			"library" to "jvm-ktor",
			"dateLibrary" to "java8",
			"serializationLibrary" to "kotlinx_serialization",
		)
	)
}*/

kotlin.sourceSets.named("main") {
	kotlin.srcDir("${layout.buildDirectory.get()}/generated/src/main/kotlin")
}
