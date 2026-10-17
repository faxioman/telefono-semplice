plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.paparazzi")
    id("com.github.triplet.play")
}

android {
    namespace = "it.rapidosoft.semplice"
    // Paparazzi 1.x (screenshot) non regge android.jar 36: per i soli screenshot si compila
    // contro la 35 con -PscreenshotSdk=35. Il codice non usa API della 36.
    compileSdk = (findProperty("screenshotSdk") as String?)?.toInt() ?: 36

    defaultConfig {
        applicationId = "it.rapidosoft.semplice"
        minSdk = 29
        targetSdk = 36
        versionCode = 7
        versionName = "0.6.0"
    }

    // Chiave di firma (anche per il Play Store): mai nel repo, arriva da variabili
    // d'ambiente / GitHub Secrets. Senza, la release resta firmata con la chiave di debug.
    val keystorePath = System.getenv("SIGNING_KEYSTORE")
    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            }
        }
    }

    // full = versione GitHub (SMS nelle impostazioni, link donazioni);
    // play = versione Play Store, senza ciò che le policy di Google non ammettono.
    flavorDimensions += "distribution"
    productFlavors {
        create("full") {
            dimension = "distribution"
            buildConfigField("boolean", "STORE_BUILD", "false")
        }
        create("play") {
            dimension = "distribution"
            buildConfigField("boolean", "STORE_BUILD", "true")
        }
    }

    playConfigs {
        // la versione GitHub non va mai sul Play Store
        register("full") { enabled.set(false) }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    androidResources { generateLocaleConfig = true }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.04.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    testImplementation("junit:junit:4.13.2")
}

// Pubblicazione sul Play Store (solo flavor "play"): scheda e grafica in src/play/play/.
// Attivo solo con le credenziali (CI di pubblicazione): le build normali non toccano Google.
val playCredentials: String? = System.getenv("PLAY_SERVICE_ACCOUNT_JSON")
play {
    enabled.set(playCredentials != null)
    playCredentials?.let { serviceAccountCredentials.set(file(it)) }
    defaultToAppBundles.set(true)
    track.set("internal")
    // versionCode = ultimo presente sul Play Store + 1
    resolutionStrategy.set(com.github.triplet.gradle.androidpublisher.ResolutionStrategy.AUTO)
}
