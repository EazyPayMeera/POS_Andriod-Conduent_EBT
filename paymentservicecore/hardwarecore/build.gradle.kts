plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("org.jetbrains.kotlin.kapt")
}
configurations.maybeCreate("default")

val hwType: String = project.findProperty("HW_TYPE") as? String ?: "UNKNOWN"

android {
    namespace = "com.eazypaytech.hardwarecore"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField ("String", "HW_TYPE", "\"$hwType\"")
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java")

            // Add platform-specific dirs based on HW_TYPE
            when (hwType) {
                "MOREFUN" -> {
                    java.srcDir("src/morefun/java")
                    manifest.srcFile("src/morefun/AndroidManifest.xml")
                }
                else -> {
                    java.srcDir("src/urovo/java")
                    manifest.srcFile("src/urovo/AndroidManifest.xml")
                }
            }
        }
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