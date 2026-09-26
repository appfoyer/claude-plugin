plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
}

kotlin {
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework { baseName = "ComposeApp" }
    }
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(libs.gitlive.firebase.auth)
            implementation(libs.sentry.kmp)
        }
        androidMain.dependencies {
            implementation(libs.play.services.location)
        }
    }
}

android {
    namespace = "org.example.pacemate"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.pacemate"
        minSdk = 24
        targetSdk = 35
    }
}
