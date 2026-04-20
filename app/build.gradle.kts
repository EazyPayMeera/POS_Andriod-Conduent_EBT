plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
    id("dagger.hilt.android.plugin")
}

val hwType: String = project.findProperty("HW_TYPE") as? String ?: "UNKNOWN"

android {
    namespace = "com.eazypaytech.pos"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.eazypaytech.pos"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.15"



        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField ("Integer", "LOG_LEVEL", "2")
        buildConfigField ("String", "ACQUIRER_NAME", "\"LYRA\"")
        buildConfigField ("String", "HW_TYPE", "\"$hwType\"")

        manifestPlaceholders["enableMoreFunService"] = (hwType == "MOREFUN").toString()
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

        }
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core libraries
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime.android)
    implementation(libs.androidx.core.animation)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.foundation.android)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    // Accompanist libraries (latest versions)
    implementation(libs.accompanist.pager)
    implementation("com.google.accompanist:accompanist-pager:0.28.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.28.0")

    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt:coil-svg:2.4.0") // or the latest version

    // Compose UI libraries
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material3:material3-window-size-class:1.2.1")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.3.0-beta04")
    implementation("androidx.compose.ui:ui:1.4.0")
    implementation("androidx.compose.material:material:1.4.0")
    implementation("androidx.compose.animation:animation:1.5.0")

    implementation("androidx.compose.material3:material3:1.0.0")
    implementation("androidx.compose.material:material-icons-extended:1.0.0")

    implementation("io.coil-kt:coil:2.2.2") // or the latest version
    implementation("io.coil-kt:coil-gif:2.2.2") // or the latest version
    implementation("io.coil-kt:coil-compose:2.2.2")

    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-android-compiler:2.51")
    //implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    // ZXing core
    implementation("com.google.zxing:core:3.5.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("androidx.compose.ui:ui-graphics:1.5.0")
    // Coil SVG decoder
    implementation("io.coil-kt:coil-svg:2.2.2")
    implementation("androidx.compose.material:material:1.5.0") // Use the latest version
    api(project(":paymentservicecore"))
    api(project(":paymentservicecore:securityframework"))
    api(project(":paymentservicecore:hardwarecore"))
   // implementation(libs.google.gson)


}

