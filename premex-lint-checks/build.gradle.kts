import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    // Run lint on the lints! https://groups.google.com/g/lint-dev/c/q_TVEe85dgc
    alias(libs.plugins.lint)
    alias(libs.plugins.mavenPublish)
}

val PUBLISH_GROUP_ID: String by extra("se.premex.premex-lints")
val PUBLISH_VERSION: String by extra(rootProject.version as String)
val PUBLISH_ARTIFACT_ID by extra("premex-lint-checks")

apply(from = "${rootProject.projectDir}/gradle/publish-module.gradle")

lint {
    htmlReport = true
    xmlReport = true
    textReport = true
    absolutePaths = false
    checkTestSources = true
    baseline = file("lint-baseline.xml")
}

dependencies {
    compileOnly(libs.bundles.lintApi)
    testImplementation(libs.bundles.lintTest)
    testImplementation(libs.junit)
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        // Lint still requires 1.4 (regardless of what version the project uses), so this forces a lower
        // language level for now. Similar to `targetCompatibility` for Java.
        apiVersion = "1.4"
        languageVersion = "1.4"
    }
}

pluginManager.withPlugin("java") {
    configure<JavaPluginExtension> { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

    tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
}

pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "11"
            // TODO re-enable once lint uses Kotlin 1.5
            //        allWarningsAsErrors = true
            //        freeCompilerArgs = freeCompilerArgs + listOf("-progressive")
        }
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach { jvmTarget = "11" }
