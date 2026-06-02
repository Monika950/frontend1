plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.treasurehuntapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.treasurehuntapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        
        // Load Google Maps API key from local.properties
        val localProperties = org.jetbrains.kotlin.konan.properties.Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { localProperties.load(it) }
        }
        val mapsApiKey = localProperties.getProperty("MAPS_API_KEY") ?: ""
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildFeatures {
        compose = true
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"http://178.104.200.100:3000/\"")
            buildConfigField("String", "SOCKET_BASE_URL", "\"http://178.104.200.100:3000\"")
        }
        create("qa") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"http://178.104.200.100:3000/\"")
            buildConfigField("String", "SOCKET_BASE_URL", "\"http://178.104.200.100:3000\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://178.104.200.100:3000/\"")
            buildConfigField("String", "SOCKET_BASE_URL", "\"https://178.104.200.100:3000\"")
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
    // Keep core versions compatible with compileSdk 35 / AGP 8.6.1
    constraints {
        implementation("androidx.core:core-ktx:1.16.0") {
            version { strictly("1.16.0") }
        }
        implementation("androidx.core:core:1.16.0") {
            version { strictly("1.16.0") }
        }
    }

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.preview)
    implementation(libs.material3)
    implementation(libs.appcompat)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.compose.foundation)
    debugImplementation(libs.compose.tooling)

    implementation(libs.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.compose)

    testImplementation(libs.junit)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation(libs.core.ktx)

    implementation(libs.material.icons.extended)
    implementation(libs.coil.compose)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx)
    implementation(libs.retrofit.gson)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.socketio.client) {
        exclude(group = "org.json", module = "json")
    }
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.maps.compose)

}
