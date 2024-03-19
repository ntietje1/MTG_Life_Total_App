plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

buildscript {
    dependencies {
        classpath("dev.icerock.moko:resources-generator:0.23.0")
    }
}