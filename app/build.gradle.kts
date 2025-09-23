import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.21"
    id("com.google.gms.google-services")
}


val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))
// local.properties 읽기
val localPropertiesFile = rootProject.file("local.properties")
val localProps = Properties().apply {
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}
val webClientId: String = localProps.getProperty("webClientId") ?: ""
android {
    namespace = "com.trever.android"
    compileSdk = 36

    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }


    defaultConfig {
        applicationId = "com.trever.android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField(
            "String",
            "WEB_CLIENT_ID",
            "\"$webClientId\""
        )
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
//    implementation("androidx.compose.material:material-icons-extended:<최신_버전>")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.compose.material:material-icons-core:1.6.8")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.compose.material3:material3:1.3.2")

    // Navigation-Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Activity Compose - 1.9.2 버전 직접 명시 삭제하여 충돌 해결
    // implementation("androidx.activity:activity-compose:1.9.2")
    implementation("io.coil-kt:coil-compose:2.6.0")

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")

    implementation("androidx.compose.ui:ui-text")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))


    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.firebase:firebase-database-ktx:20.3.0")

    implementation("androidx.compose.material:material:1.5.4")

    // 구글 로그인
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Koin 의존성 추가
    implementation("io.insert-koin:koin-core:3.5.0")
    implementation("io.insert-koin:koin-android:3.5.0")
    implementation("io.insert-koin:koin-androidx-compose:3.5.0")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}