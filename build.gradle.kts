// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.moneymanager.root)
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.google.firebase.crashlytics) apply false
}

subprojects {
    configurations.all {
        resolutionStrategy {
            // Fix Hilt compatibility with Kotlin 2.4.0 metadata
            force("org.jetbrains.kotlin:kotlin-metadata-jvm:2.4.0")
            dependencySubstitution {
                substitute(module("androidx.savedstate:savedstate-ktx"))
                    .using(module("androidx.savedstate:savedstate:1.5.0"))
                    .because("savedstate-ktx was merged into savedstate and 1.3.0 ktx doesn't exist")
            }
        }
    }
}
