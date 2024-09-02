plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.analogics.securityframework"
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

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.room.runtime) // or the latest version
    kapt("androidx.room:room-compiler:2.6.1")
    // Hilt dependencies
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-android-compiler:2.51")
    //implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

}