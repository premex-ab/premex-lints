pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        if (System.getenv("DEP_OVERRIDES") == "true") {
            val overrides = System.getenv().filterKeys { it.startsWith("DEP_OVERRIDE_") }
            maybeCreate("libs").apply {
                for ((key, value) in overrides) {
                    val catalogKey = key.removePrefix("DEP_OVERRIDE_").toLowerCase()
                    println("Overriding $catalogKey with $value")
                    version(catalogKey, value)
                }
            }
        }
    }
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "premex-lints"

include(":premex-lint-checks")
