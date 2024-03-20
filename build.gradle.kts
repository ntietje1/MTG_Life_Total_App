plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    id("dev.icerock.mobile.multiplatform-resources") version "0.23.0" apply false
}

buildscript {
//    repositories {
//        gradlePluginPortal()
//    }

    dependencies {
        classpath("dev.icerock.moko:resources:0.23.0")
    }
}
//
//allprojects {
//    repositories {
//        mavenCentral()
//    }
//}