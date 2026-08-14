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

rootProject.name = "GuardDroid"
include(":app")
include(":core:common")
include(":core:device")
include(":core:management")
include(":core:security")
include(":core:scheduling")
include(":core:database")
include(":feature:setup")
include(":feature:apps")
include(":feature:admin")
include(":feature:restrictions")
include(":core:update")
