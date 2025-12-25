import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
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
    compilerOptions {
        // Kotlin 2.3.0 requires at least version 2.0. Previously this was set to 1.4 for lint compatibility,
        // but that version is no longer supported.
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    }
}

pluginManager.withPlugin("java") {
    configure<JavaPluginExtension> { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

    tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
}

pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            // TODO re-enable once lint uses Kotlin 1.5
            //        allWarningsAsErrors = true
            //        freeCompilerArgs = freeCompilerArgs + listOf("-progressive")
        }
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach { jvmTarget = "11" }
