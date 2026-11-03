plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}
apply(plugin = "realm-android")

android {
    namespace = "io.openim.android.ouicore"
    compileSdk = libs.versions.compileSdk.get().toInt()

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    api(libs.easywindow)
    // openIM sdk
    api(libs.openim.core.sdk)
    api(libs.openim.android.sdk)
    implementation(libs.core.ktx)
    implementation(libs.kotlin.stdlib.jdk7)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.espresso.core)

    api(libs.rxjava)
    implementation(libs.retrofit.adapter.rxjava2)
    api(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    api(libs.circularprogressbar)

    api(libs.fastjson)
    api(libs.avi)
    api(libs.glide)
    api(libs.pictureselector)
    api(libs.ucrop)
    api(libs.tinypinyin)
    api(libs.tinypinyin.lexicons)

    api(libs.recyclerview.x)
    api(libs.xxpermissions)

    api(libs.shadowlayout)
    api(libs.sortletter)

    implementation(libs.therouter)
    kapt(libs.therouter.apt)

    implementation(libs.gson)

    api(libs.pickerview)
    api(libs.watermarkview)
    api(libs.roundimageview)
    api(libs.flowlayoutmanager)

    api(libs.emoji.android)
    api(libs.emoji.google)

    api(libs.okdownload)
    api(libs.okdownload.sqlite)

    api(libs.badgeview)

    api(libs.bugly)
    api(libs.autoupdate)
    api(libs.timberkt)
}
