import org.gradle.api.artifacts.VersionCatalogsExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    // buildscript 块内无法使用类型安全的 libs 访问器，改为从版本目录读取
    val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

    dependencies {
        classpath("io.realm:realm-gradle-plugin:${libsCatalog.findVersion("realm").get()}")
        classpath("cn.therouter:plugin:${libsCatalog.findVersion("therouter").get()}")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    //alias(libs.plugins.google.services) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
