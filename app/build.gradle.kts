plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.analogics.tpaymentsapos"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.analogics.tpaymentsapos"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
    implementation(project(":paymentservicecore:securityframework"))
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
	implementation (libs.accompanist.pager)
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

    implementation ("androidx.compose.material3:material3:1.0.0")
    implementation ("androidx.compose.material:material-icons-extended:1.0.0")

    implementation ("io.coil-kt:coil:2.2.2") // or the latest version
    implementation ("io.coil-kt:coil-gif:2.2.2") // or the latest version
    implementation ("io.coil-kt:coil-compose:2.2.2")


    implementation ("androidx.compose.ui:ui:1.4.0")
    implementation ("androidx.compose.material3:material3:1.0.1")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.4.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation ("androidx.activity:activity-compose:1.7.2")

    // Coil SVG decoder
    implementation ("io.coil-kt:coil-svg:2.2.2")
    implementation ("androidx.compose.material:material:1.5.0") // Use the latest version
	api(project(":paymentservicecore"))
}

