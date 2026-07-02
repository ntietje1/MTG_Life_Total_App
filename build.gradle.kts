plugins {
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.gradlePlayPublisher) apply false
}

val releasePropertiesFile = layout.projectDirectory.file("release.properties")
val iosProjectFile = layout.projectDirectory.file("iosApp/LifeLinkedIOS.xcodeproj/project.pbxproj")

fun loadReleaseProperties(): java.util.Properties {
    return java.util.Properties().apply {
        releasePropertiesFile.asFile.inputStream().use(::load)
    }
}

fun java.util.Properties.requiredReleaseProperty(name: String): String {
    return requireNotNull(getProperty(name)) {
        "Missing $name in release.properties"
    }
}

fun replaceIosVersionSetting(iosProject: String, name: String, value: String): String {
    val setting = Regex("""$name = [^;]+;""")
    require(setting.containsMatchIn(iosProject)) {
        "Missing $name in ${iosProjectFile.asFile.path}"
    }
    return setting.replace(iosProject, "$name = $value;")
}

val syncReleaseVersion by tasks.registering {
    inputs.file(releasePropertiesFile)
    outputs.file(iosProjectFile)

    doLast {
        val releaseProperties = loadReleaseProperties()
        val versionCode = releaseProperties.requiredReleaseProperty("versionCode")
        val versionName = releaseProperties.requiredReleaseProperty("versionName")

        var iosProject = iosProjectFile.asFile.readText()
        iosProject = replaceIosVersionSetting(
            iosProject = iosProject,
            name = "CURRENT_PROJECT_VERSION",
            value = versionCode
        )
        iosProject = replaceIosVersionSetting(
            iosProject = iosProject,
            name = "MARKETING_VERSION",
            value = versionName
        )

        iosProjectFile.asFile.writeText(iosProject)
    }
}

tasks.register("verifyReleaseVersion") {
    inputs.file(releasePropertiesFile)
    inputs.file(iosProjectFile)
    mustRunAfter(syncReleaseVersion)

    doLast {
        val releaseProperties = loadReleaseProperties()
        val versionCode = releaseProperties.requiredReleaseProperty("versionCode")
        val versionName = releaseProperties.requiredReleaseProperty("versionName")
        val iosProject = iosProjectFile.asFile.readText()

        require("CURRENT_PROJECT_VERSION = $versionCode;" in iosProject) {
            "iOS CURRENT_PROJECT_VERSION must match release.properties versionCode=$versionCode"
        }
        require("MARKETING_VERSION = $versionName;" in iosProject) {
            "iOS MARKETING_VERSION must match release.properties versionName=$versionName"
        }
    }
}
