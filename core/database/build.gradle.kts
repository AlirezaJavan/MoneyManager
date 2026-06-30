plugins {
    alias(libs.plugins.moneymanager.android.library)
    alias(libs.plugins.moneymanager.android.room)
    alias(libs.plugins.moneymanager.hilt)
}

android {
    namespace = "com.javanapps.moneymanager.core.database"
}

dependencies {
    api(projects.core.model)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
