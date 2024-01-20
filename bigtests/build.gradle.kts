plugins {
    alias(libs.plugins.com.android.test)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    kotlin("kapt")
}

android {
    namespace = "org.berendeev.bigtests"
    compileSdk = 34

    defaultConfig {
        minSdk = 30
        testHandleProfiling = true
        testFunctionalTest = true
        testInstrumentationRunner = "org.berendeev.bigtests.CustomTestRunner"
        testInstrumentationRunnerArguments += "useTestStorageService" to "true"
    }

    testOptions {
// now the android tests never finish if uncomment following lines
//        emulatorSnapshots {
//            enableForTestFailures = true
//            maxSnapshotsForTestFailures = 2
//        }
        animationsDisabled = false
        managedDevices {
            localDevices {
                create("mainDevice") {
                    // Use device profiles you typically see in Android Studio.
                    device = "Pixel 2"
                    // ATDs currently support only API level 30.
                    apiLevel = 33
                    // You can also specify "google-atd" if you require Google Play Services.
                    systemImageSource = "google-atd"
                }
            }
        }
    }
    kotlin {
        jvmToolchain(17)
    }

    kotlinOptions{
        freeCompilerArgs += "-Xcontext-receivers"
    }

    targetProjectPath = ":app"
}

dependencies {
    implementation(project(":app"))
    implementation(libs.core.ktx)
    implementation(libs.junit)
    implementation(libs.ext.junit)
    implementation(libs.espresso.core)
    implementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.ui.test.junit4)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.mock)

    implementation(libs.hilt.android.testing)
    kapt(libs.hilt.android.compiler)
    androidTestUtil(libs.androidx.test.services)
}
