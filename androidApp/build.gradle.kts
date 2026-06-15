import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

val releaseProperties = Properties().apply {
    rootProject.file("release.properties").inputStream().use(::load)
}

fun releaseProperty(name: String): String {
    return requireNotNull(releaseProperties.getProperty(name)) {
        "Missing $name in release.properties"
    }
}

fun signingInput(propertyName: String, environmentName: String): String? {
    return providers.gradleProperty(propertyName)
        .orElse(providers.environmentVariable(environmentName))
        .orNull
        ?.takeIf { it.isNotBlank() }
}

val releaseKeystoreFile = signingInput(
    propertyName = "lifelinked.android.keystore.file",
    environmentName = "LIFELINKED_ANDROID_KEYSTORE_FILE"
)
val releaseKeystorePassword = signingInput(
    propertyName = "lifelinked.android.keystore.password",
    environmentName = "LIFELINKED_ANDROID_KEYSTORE_PASSWORD"
)
val releaseKeyAlias = signingInput(
    propertyName = "lifelinked.android.key.alias",
    environmentName = "LIFELINKED_ANDROID_KEY_ALIAS"
)
val releaseKeyPassword = signingInput(
    propertyName = "lifelinked.android.key.password",
    environmentName = "LIFELINKED_ANDROID_KEY_PASSWORD"
)
val releaseSigningConfigured = listOf(
    releaseKeystoreFile,
    releaseKeystorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { it != null }

android {
    namespace = "com.hypeapps.lifelinked"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.hypeapps.lifelinked"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = releaseProperty("versionCode").toInt()
        versionName = releaseProperty("versionName")
        testInstrumentationRunner = "com.hypeapps.lifelinked.LifeLinkedTestRunner"
    }

    signingConfigs {
        create("release") {
            if (releaseSigningConfigured) {
                storeFile = rootProject.file(releaseKeystoreFile!!)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "DebugProbesKt.bin"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
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
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.compose.ui.test.junit4)
}

val verifyReleaseSigningInputs by tasks.registering {
    doLast {
        val missing = listOfNotNull(
            "lifelinked.android.keystore.file / LIFELINKED_ANDROID_KEYSTORE_FILE".takeIf { releaseKeystoreFile == null },
            "lifelinked.android.keystore.password / LIFELINKED_ANDROID_KEYSTORE_PASSWORD".takeIf { releaseKeystorePassword == null },
            "lifelinked.android.key.alias / LIFELINKED_ANDROID_KEY_ALIAS".takeIf { releaseKeyAlias == null },
            "lifelinked.android.key.password / LIFELINKED_ANDROID_KEY_PASSWORD".takeIf { releaseKeyPassword == null }
        )
        require(missing.isEmpty()) {
            "Missing Android release signing inputs: ${missing.joinToString()}"
        }
        require(rootProject.file(releaseKeystoreFile!!).isFile) {
            "Android release keystore file does not exist: ${rootProject.file(releaseKeystoreFile!!).absolutePath}"
        }
    }
}

tasks.matching { task ->
    task.name in setOf("assembleRelease", "bundleRelease")
}.configureEach {
    dependsOn(verifyReleaseSigningInputs)
}
