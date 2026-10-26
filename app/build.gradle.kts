plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
}

val gitTagVersion = runCatching {
    val process = ProcessBuilder("git", "describe", "--tags", "--abbrev=0")
        .redirectErrorStream(true)
        .start()
    process.inputStream.bufferedReader().readText().trim()
        .also { process.waitFor() }
}.getOrNull()?.takeIf { it.isNotBlank() } ?: "dev"

android {
    namespace = "com.groupa"
    compileSdkVersion("android-37.0")

    defaultConfig {
        applicationId = "com.groupa"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = gitTagVersion
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

}

dependencies {
    implementation(project(":presentation"))
    implementation(project(":data"))
    implementation(project(":core"))
    implementation(project(":core-ui"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.serialization.json)
    kapt(libs.hilt.android.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
}
