import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    // Run lint on the lints! https://groups.google.com/g/lint-dev/c/q_TVEe85dgc
    alias(libs.plugins.lint)
    alias(libs.plugins.ksp)
    alias(libs.plugins.mavenPublish)
}


val PUBLISH_GROUP_ID: String by extra("se.premex.premex-lints")
val PUBLISH_VERSION: String by extra(rootProject.version as String)
val PUBLISH_ARTIFACT_ID by extra("core")

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
