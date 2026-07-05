plugins {
    alias(libs.plugins.moneymanager.android.application)
    alias(libs.plugins.moneymanager.android.application.compose)
    alias(libs.plugins.moneymanager.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

val signingStorePath: String? = System.getenv("SIGNING_STORE_PATH")
val signingKeyAlias: String? = System.getenv("SIGNING_KEY_ALIAS")
val signingKeyPassword: String? = System.getenv("SIGNING_KEY_PASSWORD")

android {
    namespace = "com.javanapps.moneymanager"

    defaultConfig {
        applicationId = "javan.moneymanager"
        versionCode = 19
        versionName = "2.1.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    if (!signingStorePath.isNullOrEmpty()) {
        signingConfigs {
            create("release") {
                storeFile = file(signingStorePath)
                storePassword = signingKeyPassword.orEmpty()
                keyAlias = signingKeyAlias.orEmpty()
                keyPassword = signingKeyPassword.orEmpty()
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (!signingStorePath.isNullOrEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    // Core
    implementation(projects.core.model)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.core.data)
    implementation(projects.core.domain)

    // Features
    implementation(projects.feature.auth.impl)
    implementation(projects.feature.migration.impl)
    implementation(projects.feature.home.impl)
    implementation(projects.feature.transaction.impl)
    implementation(projects.feature.categories.impl)
    implementation(projects.feature.settings.impl)
    implementation(projects.feature.reports.impl)
    implementation(projects.feature.sms.impl)

    // WorkManager + Hilt integration
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Hilt-generated code uses @CanIgnoreReturnValue from error_prone_annotations
    implementation(libs.errorprone.annotations)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)

    // Glance App Widget
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // AndroidX / Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.biometric)

    // Navigation 3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.hilt.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
