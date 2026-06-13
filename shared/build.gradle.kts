import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinSerialization)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.isFile) {
        file.inputStream().use(::load)
    }
}

val klipyApiKeyProvider = providers.gradleProperty("klipy.api.key")
    .orElse(providers.environmentVariable("KLIPY_API_KEY"))
    .orElse(localProperties.getProperty("klipy.api.key") ?: localProperties.getProperty("KLIPY_API_KEY") ?: "")

fun String.toKotlinStringLiteral(): String = buildString {
    append('"')
    for (char in this@toKotlinStringLiteral) {
        when (char) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(char)
        }
    }
    append('"')
}

val generatedApiConfigDir = layout.buildDirectory.dir("generated/lifelinkedApiConfig/commonMain/kotlin")

val generateLifeLinkedApiConfig by tasks.registering {
    outputs.dir(generatedApiConfigDir)
    outputs.upToDateWhen { false }

    doLast {
        val outputFile = generatedApiConfigDir.get().file("domain/api/LifeLinkedApiConfig.kt").asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
            package domain.api

            internal object LifeLinkedApiConfig {
                const val KLIPY_API_KEY: String = ${klipyApiKeyProvider.get().toKotlinStringLiteral()}
            }
            """.trimIndent()
        )
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "com.hypeapps.lifelinked.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources.enable = true
        withHostTestBuilder {}.configure {}

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
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
        commonMain {
            kotlin.srcDir(generatedApiConfigDir)
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.activity)

            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.viewmodel.savedstate)

            implementation(libs.androidx.lifecycle.viewmodel.ktx)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.lifecycle.livedata.ktx)

            implementation(libs.ktor.client.okhttp)

            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            implementation(libs.kotlinx.coroutines.android)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.animation)
            implementation(libs.compose.components.resources)

            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.kotlinx.coroutines.core)

            api(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(libs.ktor.client.core)

            implementation("org.slf4j:slf4j-simple:2.0.11")
            implementation(libs.kotlinx.datetime)

            implementation(libs.peekaboo.image.picker)

            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)

            implementation(libs.kamel.image.default)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

dependencies {
    "androidRuntimeClasspath"(libs.compose.ui.tooling)
}

tasks.matching { task ->
    task.name.startsWith("compile")
}.configureEach {
    dependsOn(generateLifeLinkedApiConfig)
}
