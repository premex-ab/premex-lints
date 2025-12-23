import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
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
        // Lint requires at least Kotlin 2.0 language level when using Kotlin compiler 2.3.0+
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
            //        allWarningsAsErrors.set(true)
            //        freeCompilerArgs.add("-progressive")
        }
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach { jvmTarget = "11" }
