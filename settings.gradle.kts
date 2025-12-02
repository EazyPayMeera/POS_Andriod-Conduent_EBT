pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("local-maven") }
        google()
        mavenCentral()
    }
}

rootProject.name = "EzeAndroidPOS"
include(":app")

include(":paymentservicecore")
include(":paymentservicecore:networkservicecore")
include(":paymentservicecore:builder-core")
include(":paymentservicecore:hardwarecore")
include(":paymentservicecore:securityframework")
