import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.0" apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.lint) apply false
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
    alias(libs.plugins.com.gladed.androidgitversion)
    alias(libs.plugins.com.github.ben.manes.versions)
}

androidGitVersion {
    tagPattern = "^v[0-9]+.*"
}

val gitOrLocalVersion: String =
    com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(rootDir, providers)
        .getProperty("VERSION_NAME", androidGitVersion.name().replace("v", ""))

version = gitOrLocalVersion

// https://github.com/ben-manes/gradle-versions-plugin
tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

apply(from = "${rootDir}/gradle/publish-root.gradle")