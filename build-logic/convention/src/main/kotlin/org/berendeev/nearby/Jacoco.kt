package org.berendeev.nearby

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.util.Locale

private val coverageExclusions = listOf(
    // Android
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*"
)

private fun String.capitalize() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}

fun Project.configureJacoco(
    androidComponentsExtension: AndroidComponentsExtension<*, *, *>,
) {

    tasks.register("jacocoTestReport", JacocoReport::class) {
        dependsOn("testsForCoverage")
        group = "report"
        reports {
            xml.required.set(true)
            html.required.set(true)
            html.outputLocation.set(layout.projectDirectory.dir("reports/jacoco"))
        }

        classDirectories.setFrom(
            layout.buildDirectory.map {
                it.dir("tmp/kotlin-classes/debug")
            }.map {
                it.asFileTree.matching {
                    exclude(coverageExclusions)
                }
            }
        )

        sourceDirectories.setFrom(files("$projectDir/src/main/java", "$projectDir/src/main/kotlin"))
        executionData.setFrom(
            layout.buildDirectory
                .map { dir ->
                    dir.asFileTree.matching {
                        include("**/*.ec")
                        include("**/*.exec")
                    }.files
                }
        )
    }

    tasks.withType<Test>().configureEach {
        configure<JacocoTaskExtension> {
            // Required for JaCoCo + Robolectric
            // https://github.com/robolectric/robolectric/issues/2230
            isIncludeNoLocationClasses = true

            // Required for JDK 11 with the above
            // https://github.com/gradle/gradle/issues/5184#issuecomment-391982009
            excludes = listOf("jdk.internal.*")
        }
    }
}
