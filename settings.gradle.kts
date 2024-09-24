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
        google()
        mavenCentral()
    }
}

rootProject.name = "TPaymentsAPOS"
include(":app")

include(":paymentservicecore")
include(":paymentservicecore:networkservicecore")
include(":paymentservicecore:builder-core")
include(":paymentservicecore:tpaymentcore")
include(":paymentservicecore:securityframework")
include(":paymentservicecore:mylibrary")
