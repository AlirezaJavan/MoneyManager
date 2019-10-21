plugins {
    alias(libs.plugins.moneymanager.android.library)
    alias(libs.plugins.moneymanager.hilt)
}

android {
    namespace = "com.javanapps.moneymanager.core.data"
}

dependencies {
    api(projects.core.model)
    api(projects.core.database)
    api(projects.core.datastore)
    api(projects.core.common)

    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(projects.core.testing)
}
