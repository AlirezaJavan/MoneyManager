import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.javanapps.moneymanager.buildlogic.configureKotlinAndroid
import com.javanapps.moneymanager.buildlogic.disableUnnecessaryAndroidTests
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)

                val resourcePrefix = path
                    .split(":")
                    .drop(1)
                    .joinToString(separator = "_")
                    .lowercase() + "_"
                this.resourcePrefix = resourcePrefix
            }

            extensions.configure<LibraryAndroidComponentsExtension> {
                disableUnnecessaryAndroidTests(target)
            }

            dependencies {
                "testImplementation"(kotlin("test"))
            }
        }
    }
}
