plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kapt)
}

kotlin {

    targets.all {
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

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
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.ktor.client.okhttp)

            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.animation)
            implementation(compose.components.resources)

            val nav_version = "2.7.0-alpha03"
//            implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
//            implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
            implementation(libs.navigation.compose)

            implementation(libs.androidx.lifecycle.viewmodel.ktx)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.livedata.ktx)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.savedstate)

            api(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(libs.kotlinx.serialization.json)

            implementation(libs.ktor.client.core)

            implementation("org.slf4j:slf4j-simple:2.0.11") // needed because some other dependency was causing error during bundle generating
            implementation(libs.kotlinx.datetime)

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

    defaultConfig {
        applicationId = "com.hypeapps.lifelinked"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        compileSdk = libs.versions.android.compileSdk.get().toInt()
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
        "kapt"(libs.androidx.lifecycle.compiler)
    }
}

