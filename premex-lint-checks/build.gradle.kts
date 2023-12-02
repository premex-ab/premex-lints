import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.lint)
    alias(libs.plugins.mavenPublish)
}

val versionFile = File("versions.properties")
val versions = Properties().apply {
    if (versionFile.exists()) {
        FileInputStream(versionFile).use {
            load(it)
        }
    }
}
val version = versions.getProperty("V_VERSION", "0.0.1")

val PUBLISH_GROUP_ID: String by extra("se.premex.premex-lints")
val PUBLISH_VERSION: String by extra(version as String)
val PUBLISH_ARTIFACT_ID by extra("premex-lint-checks")

apply(from = "../gradle/publish-module.gradle")

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
