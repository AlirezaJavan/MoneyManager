plugins {
    alias(libs.plugins.moneymanager.android.feature.impl)
    alias(libs.plugins.moneymanager.android.library.compose)
}

android {
    namespace = "com.javanapps.moneymanager.feature.migration.impl"
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.extended)
}
