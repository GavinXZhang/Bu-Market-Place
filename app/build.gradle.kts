plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.bumarketplace"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bumarketplace"
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-auth-ktx") // Firebase Authentication
    implementation("com.google.android.gms:play-services-auth") // Google Sign-In
    implementation("androidx.core:core-splashscreen:1.0.1") // SplashScreen
    implementation("com.airbnb.android:lottie-compose:6.6.0") // Lottie Animations
    implementation("androidx.compose.material:material-icons-extended:1.7.5") // Extended material icons
    implementation("androidx.navigation:navigation-compose:2.7.3") // Navigation
    implementation ("io.coil-kt:coil-compose:2.6.0")


    implementation("androidx.activity:activity-compose:1.9.3") // Jetpack Compose in context with Activity, intergrates with photo picker API
    implementation("androidx.compose.material3:material3:1.3.1") // Photo picker might have material3 elements
    implementation("io.coil-kt:coil-compose:2.0.0")
}