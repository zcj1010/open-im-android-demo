plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    //alias(libs.plugins.google.services)
    id("therouter")
}

android {
    namespace = "io.openim.android.demo"
    compileSdk = libs.versions.compileSdk.get().toInt()

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        applicationId = libs.versions.applicationId.get()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // 原 Groovy 写法未实际赋值，保持行为不变
        }
    }

    signingConfigs {
        create("release") {
            keyAlias = "openim"
            keyPassword = "openim"
            storeFile = file("./openim")
            storePassword = "openim"
        }
    }
    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("release")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.legacy.support.v4)
    implementation(libs.lifecycle.livedata.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.multidex)

    implementation(libs.therouter)
    kapt(libs.therouter.apt)
    implementation(libs.verificationcodeinputview)

    implementation(libs.badgebutton)
    implementation(libs.ccp)
    // FCM 推送已移除，保留供后期参考（恢复时需同时恢复 google-services 插件与 app/google-services.json）
    //implementation(platform(libs.firebase.bom))
    //implementation("com.google.firebase:firebase-messaging") // FCM
    //implementation("com.google.firebase:firebase-analytics") // Optional for FCM
    //--------
    api(project(":OUICore"))
    implementation(project(":OUIConversation"))
    implementation(project(":OUIGroup"))
    implementation(project(":OUIContact"))
    implementation(project(":OUICalling"))
}
