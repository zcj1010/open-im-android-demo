pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()

        maven("https://maven.aliyun.com/nexus/content/groups/public/")
        maven("https://maven.aliyun.com/nexus/content/repositories/jcenter")
        maven("https://maven.aliyun.com/nexus/content/repositories/google")
        maven("https://maven.aliyun.com/nexus/content/repositories/gradle-plugin")
        maven("https://jitpack.io")
        maven("https://mvn.getui.com/nexus/content/repositories/releases/")
        maven("https://developer.huawei.com/repo/")
        maven {
            // r8 maven
            url = uri("https://storage.googleapis.com/r8-releases/raw")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()

        maven("https://maven.aliyun.com/nexus/content/groups/public/")
        maven("https://maven.aliyun.com/nexus/content/repositories/jcenter")
        maven("https://maven.aliyun.com/nexus/content/repositories/google")
        maven("https://maven.aliyun.com/nexus/content/repositories/gradle-plugin")
        maven("https://jitpack.io")
        maven("https://mvn.getui.com/nexus/content/repositories/releases/")
        maven("https://developer.huawei.com/repo/")
        maven {
            // r8 maven
            url = uri("https://storage.googleapis.com/r8-releases/raw")
        }
    }
}
rootProject.name = "Demo"
include(":app")

include(":OUICore")
include(":OUIConversation")
include(":OUIGroup")
include(":OUIContact")
include(":OUICalling")
