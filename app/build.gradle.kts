import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val ksProps = Properties().also { props ->
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { props.load(it) }
    }
}

val ksStoreFile: String? = ksProps.getProperty("storeFile")
    ?: if (System.getenv("RELEASE_STORE_PASSWORD") != null) "release-keystore.jks" else null
val ksStorePassword: String? = ksProps.getProperty("storePassword") ?: System.getenv("RELEASE_STORE_PASSWORD")
val ksKeyAlias: String? = ksProps.getProperty("keyAlias") ?: System.getenv("RELEASE_KEY_ALIAS")
val ksKeyPassword: String? = ksProps.getProperty("keyPassword") ?: System.getenv("RELEASE_KEY_PASSWORD")

android {
    namespace = "com.majorbriggs.metronome"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.majorbriggs.metronome"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            if (ksStoreFile != null) {
                storeFile = file(ksStoreFile)
                storePassword = ksStorePassword
                keyAlias = ksKeyAlias
                keyPassword = ksKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
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
    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.wear.compose.navigation)
    implementation(libs.wear.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.datastore.preferences)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.wear)
    implementation(libs.wear.ongoing)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}
