pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MoneyManager"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Enforce JDK 17+
check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    "This project requires JDK 17+. Current: ${JavaVersion.current()}"
}

// Core modules
include(":core:model")
include(":core:common")
include(":core:designsystem")
include(":core:ui")
include(":core:database")
include(":core:datastore")
include(":core:data")
include(":core:domain")
include(":core:testing")

// Feature modules
include(":feature:auth:impl")
include(":feature:migration:impl")
include(":feature:home:impl")
include(":feature:transaction:impl")
include(":feature:categories:impl")
include(":feature:settings:impl")
include(":feature:reports:impl")
include(":feature:sms:impl")

// App
include(":app")
