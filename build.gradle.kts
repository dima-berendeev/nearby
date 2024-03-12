plugins {
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.com.android.aplication) apply false
    alias(libs.plugins.com.android.test) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    kotlin("plugin.serialization") version "1.8.10" apply false
    id("io.ktor.plugin") version "2.3.5"
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
    id("io.github.takahirom.roborazzi") version "1.8.0-rc-2" apply false
}

subprojects {
    afterEvaluate {
        tasks.named("clean") {
            doLast {
                layout.projectDirectory.dir("reports").asFile.deleteRecursively()
            }
        }
    }
}
