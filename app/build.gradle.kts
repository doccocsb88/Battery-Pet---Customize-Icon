plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val versionCodeBase = (findProperty("VERSION_CODE_BASE") as String?)?.toIntOrNull() ?: 1000
val fallbackVersionCode = (findProperty("VERSION_CODE") as String?)?.toIntOrNull() ?: 1
val computedVersionCode = System.getenv("GITHUB_RUN_NUMBER")
    ?.toIntOrNull()
    ?.let { versionCodeBase + it }
    ?: fallbackVersionCode

val signingStoreFilePath = System.getenv("SIGNING_KEY_STORE_FILE")
    ?: (findProperty("SIGNING_KEY_STORE_FILE") as String?)
val signingStorePassword = System.getenv("SIGNING_STORE_PASSWORD")
    ?: (findProperty("SIGNING_STORE_PASSWORD") as String?)
val signingKeyAlias = System.getenv("SIGNING_KEY_ALIAS")
    ?: (findProperty("SIGNING_KEY_ALIAS") as String?)
val signingKeyPassword = System.getenv("SIGNING_KEY_PASSWORD")
    ?: (findProperty("SIGNING_KEY_PASSWORD") as String?)
val hasReleaseSigning = listOf(
    signingStoreFilePath,
    signingStorePassword,
    signingKeyAlias,
    signingKeyPassword,
).all { !it.isNullOrBlank() }

android {
    namespace = "co.q7labs.co.emoji"
    compileSdk = 35

    defaultConfig {
        applicationId = "co.q7labs.co.emoji"
        minSdk = 26
        targetSdk = 35
        versionCode = computedVersionCode
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        // Ship English locale variants only; base `values/` stays included (see AppLanguageConfig).
        resourceConfigurations += listOf("en")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(signingStoreFilePath!!)
                storePassword = signingStorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    assetPacks += ":store_pack"
}

dependencies {
    val bom = platform("androidx.compose:compose-bom:2024.10.01")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("com.google.android.material:material:1.12.0")
    implementation(bom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.android.billingclient:billing-ktx:7.1.1")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil:2.6.0")
    implementation("io.coil-kt:coil-gif:2.6.0")
    implementation("com.airbnb.android:lottie-compose:6.4.0")
    implementation("com.google.android.play:asset-delivery:2.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
