plugins {
    alias(libs.plugins.moneymanager.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.shamsi.core)
    implementation(libs.kotlinx.serialization.json)
}
