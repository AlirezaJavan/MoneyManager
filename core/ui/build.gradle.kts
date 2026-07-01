plugins {
    alias(libs.plugins.moneymanager.android.library)
    alias(libs.plugins.moneymanager.android.library.compose)
}

android {
    namespace = "com.javanapps.moneymanager.core.ui"
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)
    api(libs.shamsi.picker)

    testImplementation(libs.junit)
    testImplementation(libs.truth)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
