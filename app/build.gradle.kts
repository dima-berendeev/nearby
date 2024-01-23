plugins {
    alias(libs.plugins.com.android.aplication)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.kover")
    id("io.github.takahirom.roborazzi")
}

android {
    namespace = "org.berendeev.nearby"
    compileSdk = 34
    testOptions {
        animationsDisabled = true
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
    defaultConfig {
        applicationId = "org.berendeev.nearby"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments += "useTestStorageService" to "true"
        vectorDrawables {
            useSupportLibrary = true
        }
        val apiKey = project.properties["FOURSQUARE_API_KEY"]
            ?: System.getenv("FOURSQUARE_API_KEY")
        buildConfigField("String", "API_KEY", "\"${apiKey}\"")
    }
    buildTypes {
        debug {

// todo uncomment for Android Studio Iguana/AGP 8.3 .
// https://issuetracker.google.com/issues/281266702
//            enableAndroidTestCoverage = true
//            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtensionVersion.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
    }
    lint {
        htmlReport = true
        htmlOutput = file("${projectDir}/reports/lint/index.html")
    }
}

koverReport {
    androidReports("debug") {
        filters {
            excludes {
                classes("*BuildConfig*")
                classes("*ComposableSingletons*")
                classes("*hilt*")
                classes("*Hilt*")
                annotatedBy("*Generated*")
                annotatedBy("*Preview*")
                annotatedBy("*Serializable*")
            }
        }
    }
}

dependencies {
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    kapt(libs.hilt.android.compiler)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material)
    debugImplementation(libs.androidx.compose.ui.tooling)

    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.ktor.client.android)
    implementation(libs.coil.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.play.services.location)
    implementation(libs.kotlin.coroutines.play)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.kotlinTest)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.android.compiler)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.io.mockk)
    testImplementation(libs.mockk.android)
    testImplementation(libs.mockk.agent)
}
