plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.8.20"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.20")
    implementation("com.android.tools.build:gradle:8.1.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

gradlePlugin {
    plugins {
        register("androidApplicationJacoco") {
            id = "nearby.android.application.jacoco"
            implementationClass = "org.berendeev.nearby.AndroidApplicationJacocoConventionPlugin"
        }
    }
}
