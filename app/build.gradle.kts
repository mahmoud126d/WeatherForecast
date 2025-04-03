plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin ("plugin.serialization") version "2.1.10"
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.weatherforecast"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.weatherforecast"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(libs.play.services.location)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Scoped API
    implementation(libs.androidx.lifecycle.viewmodel.compose.android)
    //LiveData
    implementation (libs.androidx.runtime.livedata)

    //compose navigation
    implementation (libs.androidx.navigation.compose)
    //Serialization for NavArgs
    implementation (libs.kotlinx.serialization.json)

    //Room
    implementation (libs.androidx.room.ktx)
    implementation (libs.androidx.room.runtime)
    //ksp(libs.androidx.room.compiler)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    //GSON
    ksp("androidx.room:room-compiler:2.6.1")

    //navigation
    //implementation(libs.androidx.navigation.compose.v289)
    //pull to refresh
    implementation (libs.accompanist.swiperefresh)
    //for make content extend on top of screen
    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
    //data store
    implementation("androidx.datastore:datastore-preferences:1.1.3")
    //navigation
    implementation("androidx.navigation:navigation-compose:2.8.8")
    //google maps
    implementation ("com.google.maps.android:maps-compose:2.11.4")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    //lottie
    implementation("com.airbnb.android:lottie-compose:6.3.0")
    //material
    implementation ("androidx.compose.material:material:1.6.0")
    //work manger
    implementation ("androidx.work:work-runtime-ktx:2.7.1")

// Dependencies for local unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.robolectric:robolectric:4.5.1")

// AndroidX Test - JVM testing
    testImplementation("androidx.test:core-ktx:1.6.1")
 testImplementation("androidx.test.ext:junit:1.1.3")

// AndroidX Test - Instrumented testing
    androidTestImplementation("androidx.test:core:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

// Timber
    implementation("com.jakewharton.timber:timber:5.0.1")

// Hamcrest
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.hamcrest:hamcrest-library:2.2")
    androidTestImplementation("org.hamcrest:hamcrest:2.2")
    androidTestImplementation("org.hamcrest:hamcrest-library:2.2")

// AndroidX and Robolectric
    testImplementation("androidx.test.ext:junit-ktx:1.1.3")
    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("org.robolectric:robolectric:4.11")

// InstantTaskExecutorRule
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")

// kotlinx-coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.0")

// MockK
    testImplementation("io.mockk:mockk-android:1.13.17")
    testImplementation("io.mockk:mockk-agent:1.13.17")
    testImplementation("io.mockk:mockk:1.13.7")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.mockito:mockito-core:5.7.0")

// WorkManager
    implementation("androidx.work:work-runtime-ktx:2.7.1")

}