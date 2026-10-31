import java.util.Properties
plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
}



val keystorePropsFile = rootProject.file("uni.uni")
val keystoreProps = Properties().apply {
  if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}
android {
  namespace = "obachan.aaa"
  compileSdk {
    version = release(36)
  }

  defaultConfig {
    applicationId = "obachan.aaa"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0.2"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      if (keystorePropsFile.exists()) {
        storeFile = file(keystoreProps["storeFile"] as String)
        storePassword = keystoreProps["storePassword"] as String
        keyAlias = keystoreProps["keyAlias"] as String
        keyPassword = keystoreProps["keyPassword"] as String
      }
    }
  }
  buildTypes {
    release {
      signingConfig = signingConfigs.getByName("release")
      isMinifyEnabled = true
      isShrinkResources = true
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
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}