plugins {
    alias(libs.plugins.com.android.test)
    alias(libs.plugins.org.jetbrains.kotlin.android)
}

android {
    namespace = "org.berendeev.bigtests"
    compileSdk = 34

    defaultConfig {
        minSdk = 30
        testHandleProfiling = true
        testFunctionalTest = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    kotlin {
        jvmToolchain(11)
    }

    targetProjectPath = ":app"
}

dependencies {
    implementation(project(":app"))
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.junit)
    implementation(libs.ext.junit)
    implementation(libs.espresso.core)
    implementation("androidx.test:runner:1.4.0")
    implementation("androidx.test:rules:1.4.0")
}
