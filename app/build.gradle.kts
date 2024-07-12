import java.io.FileInputStream
import java.util.Properties
import com.android.build.api.variant.FilterConfiguration.FilterType.ABI

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version "1.9.0"
}

val keystoreProperties = Properties()
val keystorePropertiesFile: File = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.illusionman1212.lyricsgrabbr"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.illusionman1212.lyricsgrabbr"
        minSdk = 23
        targetSdk = 34
        versionCode = 6
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        signingConfig = signingConfigs.getByName("debug")

        // https://developer.android.com/guide/topics/resources/app-languages#gradle-config
        resourceConfigurations.plus(listOf("en", "ar"))
    }

    signingConfigs {
        create("release") {
            storeFile = keystoreProperties["storeFile"]?.let { file(it)}
            storePassword = keystoreProperties["storePassword"] as String?
            keyAlias = keystoreProperties["keyAlias"] as String?
            keyPassword = keystoreProperties["keyPassword"] as String?
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")

            // https://developer.android.com/build/configure-apk-splits#configure-abi-split
            splits {
                abi {
                    // Enables building multiple APKs per ABI.
                    isEnable = true
                    // By default all ABIs are included, so use reset() and include to specify that we only
                    // want APKs for armeabi-v7a, arm64-v8a, x86 and x86_64.
                    // Resets the list of ABIs that Gradle should create APKs for to none.
                    reset()
                    include("x86_64", "x86", "armeabi-v7a", "arm64-v8a")

                    isUniversalApk = true
                }
            }
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    androidComponents {
        val abiVersionCodes = mapOf(
            "x86_64" to 1,
            "x86" to 2,
            "armeabi-v7a" to 3,
            "arm64-v8a" to 4,
        )

        onVariants { variant ->
            // Assigns a different version code for each output APK
            // other than the universal APK.
            variant.outputs.forEach { output ->
                val name = output.filters.find { it.filterType == ABI }?.identifier

                // Stores the value of abiCodes that is associated with the ABI for this variant.
                val abiCode = abiVersionCodes[name] ?: 0
                // Assigns the new version code to output.versionCode, which changes the version code
                // for only the output APK, not for the variant itself.
                output.versionCode.set((output.versionCode.get() ?: 0) * 10 + abiCode)
            }
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("io.coil-kt:coil:2.6.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("org.jsoup:jsoup:1.17.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
