plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // alias(libs.plugins.kotlin.compose) // You might have this via libs, or directly
    id("org.jetbrains.kotlin.plugin.compose") // Ensure you have the compose plugin
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.banglagan"
    compileSdk = 35 // আপনি চাইলে আপনার SDK ভার্সন অনুযায়ী পরিবর্তন করতে পারেন

    defaultConfig {
        applicationId = "com.example.banglagan"
        minSdk = 21 // নূন্যতম SDK ভার্সন
        targetSdk = 35 // টার্গেট SDK ভার্সন
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    builbuildTypesdTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true // BuildConfig ব্যবহারের জন্য
    }
    packaging { // packagingOptions এর নতুন নাম packaging
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.com.google.android.material.material) // Material Components for Android

    // ViewModel for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Room dependencies
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7") // সর্বশেষ ভার্সন ব্যবহার করুন

    // Glance dependencies (যদি ব্যবহার করতে চান)
    // implementation("androidx.glance:glance:1.1.1")
    // implementation("androidx.glance:glance-appwidget:1.1.1")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

