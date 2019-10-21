plugins {
    alias(libs.plugins.moneymanager.android.library)
}

android {
    namespace = "com.javanapps.moneymanager.core.testing"
}

dependencies {
    api(projects.core.data)
    api(projects.core.model)
    api(libs.kotlinx.coroutines.test)
    api(libs.junit)

    implementation(libs.kotlinx.coroutines.core)
}
