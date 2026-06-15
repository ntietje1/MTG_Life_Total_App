plugins {
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinSerialization) apply false
}

val releasePropertiesFile = layout.projectDirectory.file("release.properties")
val iosProjectFile = layout.projectDirectory.file("iosApp/LifeLinkedIOS.xcodeproj/project.pbxproj")

tasks.register("verifyReleaseVersion") {
    inputs.file(releasePropertiesFile)
    inputs.file(iosProjectFile)

    doLast {
        val releaseProperties = java.util.Properties().apply {
            releasePropertiesFile.asFile.inputStream().use(::load)
        }
        val versionCode = requireNotNull(releaseProperties.getProperty("versionCode")) {
            "Missing versionCode in release.properties"
        }
        val versionName = requireNotNull(releaseProperties.getProperty("versionName")) {
            "Missing versionName in release.properties"
        }
        val iosProject = iosProjectFile.asFile.readText()

        require("CURRENT_PROJECT_VERSION = $versionCode;" in iosProject) {
            "iOS CURRENT_PROJECT_VERSION must match release.properties versionCode=$versionCode"
        }
        require("MARKETING_VERSION = $versionName;" in iosProject) {
            "iOS MARKETING_VERSION must match release.properties versionName=$versionName"
        }
    }
}
