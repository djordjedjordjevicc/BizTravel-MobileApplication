plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    //alias(libs.plugins.google.gms.google.services)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.biztravel2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.biztravel2"
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.camera:camera-core:1.2.3")
    implementation("androidx.camera:camera-camera2:1.2.3")
    implementation("androidx.camera:camera-lifecycle:1.2.3")
    implementation("androidx.camera:camera-view:1.2.3")
    implementation("androidx.camera:camera-extensions:1.2.3")
    implementation("androidx.compose.ui:ui:1.5.1")
    implementation("androidx.compose.material:material:1.5.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation ("androidx.navigation:navigation-compose:2.7.0")
    implementation ("com.google.firebase:firebase-firestore:25.0.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.maps.android:android-maps-utils:2.3.0")
    implementation("com.google.android.gms:play-services-maps:18.0.2")
    implementation ("com.google.maps.android:maps-compose:2.12.0")
    implementation ("io.coil-kt:coil-compose:2.2.2")
    implementation ("org.osmdroid:osmdroid-android:6.1.14")
    implementation ("com.google.accompanist:accompanist-coil:0.15.0")
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}