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

    kotlin {
        jvmToolchain(11)
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

    implementation("androidx.test:runner:1.4.0")
    implementation("androidx.test:rules:1.4.0")
    implementation(libs.hilt.android.testing)
    kapt(libs.hilt.android.compiler)
    androidTestUtil(libs.androidx.test.services)
}
