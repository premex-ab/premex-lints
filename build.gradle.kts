import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.lint) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.io.github.gradle.nexus.publish.plugin)
    alias(libs.plugins.com.gladed.androidgitversion)
}

androidGitVersion {
    tagPattern = "^v[0-9]+.*"
}

val gitOrLocalVersion: String =
    com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(rootDir)
        .getProperty("VERSION_NAME", androidGitVersion.name().replace("v", ""))

version = gitOrLocalVersion

subprojects {
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

    tasks.withType<Detekt>().configureEach { jvmTarget = "11" }
}

apply(from = "${rootDir}/gradle/publish-root.gradle")