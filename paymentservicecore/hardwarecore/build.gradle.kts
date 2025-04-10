plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("org.jetbrains.kotlin.kapt")
}
configurations.maybeCreate("default")

android {
    namespace = "com.eazypaytech.hardwarecore"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val hwType: String = project.findProperty("HW_TYPE") as? String ?: "UNKNOWN"

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    when (hwType) {
        "UROVO" -> {
            implementation(files("libs/urovo_sdk_v1.0.12.aar"))
        }
        "MOREFUN" -> {
            implementation(files("libs/ysdk_6.01.276894c_24123015.jar"))
            implementation(files("libs/urovo_sdk_v1.0.12.aar")) /* There are dependencies on UROVO lib. TODO: Remove these dependencies */
        }
    }

    implementation("com.google.dagger:hilt-android:2.51")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    kapt(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}