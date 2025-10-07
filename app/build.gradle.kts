plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.skolar20"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.skolar20"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"skolar-demo\"")
        buildConfigField("String", "FIREBASE_WEB_API_KEY", "\"AIzaSyCHMlF9hXblTHk_fFRHmMiquHKjopke1N0\"")
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

    // Recommended with AGP 8+
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true   // ✅ correct way
    }

    // If your setup needs it; otherwise omit (the compose plugin may manage this)
    // composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }
}

dependencies {
    // --- Navigation (Compose only) ---
    implementation("androidx.navigation:navigation-compose:2.8.1")
    // remove: implementation(libs.androidx.navigation.common.android)

    // --- HTTP + JSON ---
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")

    // --- Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    // --- Firebase (use ONE BOM + ONE Auth dep) ---
    implementation(platform("com.google.firebase:firebase-bom:34.3.0")) // keep this
    implementation("com.google.firebase:firebase-auth")             // keep this
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    // remove: implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    // remove: implementation(libs.firebase.auth.ktx)  // duplicate

    // --- AndroidX (from your version catalog) ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
