import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
    id("dev.icerock.mobile.multiplatform-resources")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            binaryOption("bundleId", "shared")
            baseName = "shared"
            isStatic = true
            export("dev.icerock.moko:resources:0.23.0")
            export("dev.icerock.moko:graphics:0.9.0")
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
//            implementation(libs.decompose)
            implementation(libs.ktor.client.okhttp)

        }
        commonMain.dependencies {
            implementation("dev.icerock.moko:resources:0.23.0")
            implementation("dev.icerock.moko:resources-compose:0.23.0")
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.kotlinx.serialization.json)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(libs.ktor.client.core)
            implementation("org.slf4j:slf4j-simple:2.0.11") // needed because some other dependency was causing error during bundle generating
            implementation(libs.kotlinx.datetime)
            implementation(libs.decompose)
            implementation(libs.decompose.extensions.compose.jetbrains)
            implementation(libs.mpfilepicker)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.hypeapps.lifelinked"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
    sourceSets["main"].java.srcDirs("build/generated/moko/androidMain/src")

    defaultConfig {
        applicationId = "com.hypeapps.lifelinked"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 4
        versionName = "1.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

// from moko
multiplatformResources {
//    resourcesPackage = "com.hypeapps.lifelinked"
//    resourcesClassName = "SharedRes"
    multiplatformResourcesPackage = "com.hypeapps.lifelinked"
//    multiplatformResourcesPackage = "com.hypeapps.lifelinked"
    multiplatformResourcesClassName = "SharedRes"
//    multiplatformResourcesVisibility = MRVisibility.Internal // optional, default Public
//    iosBaseLocalizationRegion = "en" // optional, default "en"
//    multiplatformResourcesSourceSet = "commonMain"  // optional, default "commonMain"
}
