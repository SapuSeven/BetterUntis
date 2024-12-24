import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
	implementation(libs.androidx.room.common)

	testImplementation(libs.junit)
}

/* Auto-generate api code from untis-extern spec - disabled for now, since not in use
def apiSpecList = []
def dir = new File("$projectDir/spec/untis-extern/".toString())
dir.eachFileRecurse(groovy.io.FileType.FILES) { file ->
	if (file.getName().endsWith(".yaml"))
		apiSpecList << file
}
apiSpecList.each {
	def fileName = it.getName().replace(".yaml", "");
	def taskName = fileName.split('-').collect { it.capitalize() }.join();
	def packageName = fileName.replace("-", "_");

	tasks.register("openApiGenerate" + taskName, org.openapitools.generator.gradle.plugin.tasks.GenerateTask.class, {
		generatorName = "kotlin"
		inputSpec = "$projectDir/spec/untis-extern/".toString() + "${fileName}.yaml"
		outputDir = "$buildDir/generated".toString()
		apiPackage = "com.sapuseven.untis.api.".toString() + "${packageName}"
		modelPackage = "com.sapuseven.untis.model.".toString() + "${packageName}"
		//templateDir = "$rootDir/src/main/resources/api/templates".toString()
		//    https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/jaxrs-spec.md
		configOptions = [
			library: 'jvm-ktor',
			dateLibrary: 'java8',
			serializationLibrary: 'kotlinx_serialization',
		]
	})
	compileJava.dependsOn("openApiGenerate" + taskName)
}*/

openApiGenerate {
	generatorName.set("kotlin")
	inputSpec.set("$projectDir/spec/untis.yaml")
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
}

kotlin.sourceSets.named("main") {
	kotlin.srcDir("$buildDir/generated/src/main/kotlin")
}
