import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

class RootConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jlleitschuh.gradle.ktlint")

            subprojects {
                pluginManager.apply("org.jlleitschuh.gradle.ktlint")
                extensions.configure<KtlintExtension> {
                    android.set(true)
                    ignoreFailures.set(true)
                }
            }
        }
    }
}
