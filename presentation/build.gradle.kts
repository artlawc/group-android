import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

val tiandituMapKey = System.getenv("TIANDITU_MAP_KEY")
    ?: localProperties.getProperty("tianditu.mapKey")
    ?: error("TIANDITU_MAP_KEY must be set in the environment or local.properties.")

val car3dModelUrl = System.getenv("CAR_3D_MODEL_URL")
    ?: localProperties.getProperty("car3d.modelUrl")
    ?: "https://v6.gh-proxy.org/https://raw.githubusercontent.com/artlawc/car-concept-3d-dashboard/main/CarConcept.glb"

val tiandituMapKeyLiteral = charArrayOf('"', *tiandituMapKey.toCharArray(), '"').concatToString()
val car3dModelUrlLiteral = charArrayOf('"', *car3dModelUrl.toCharArray(), '"').concatToString()

android {
    namespace = "com.groupa.presentation"
    compileSdkVersion("android-37.0")
    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            type = "String",
            name = "TIANDITU_MAP_KEY",
            value = tiandituMapKeyLiteral,
        )
        buildConfigField(
            type = "String",
            name = "CAR_3D_MODEL_URL",
            value = car3dModelUrlLiteral,
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
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
}

dependencies {
    api(project(":domain"))
    implementation(project(":core"))
    implementation(project(":core-ui"))
    testImplementation(project(":data"))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation(project(":data"))
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.osmdroid.android)
    testImplementation(libs.junit)
}
