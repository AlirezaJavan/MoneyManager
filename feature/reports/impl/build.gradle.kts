plugins {
    alias(libs.plugins.moneymanager.android.feature.impl)
    alias(libs.plugins.moneymanager.android.library.compose)
}

android {
    namespace = "com.javanapps.moneymanager.feature.reports.impl"
}

dependencies {
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
}
