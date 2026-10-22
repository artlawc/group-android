pluginManagement {
    repositories {
        maven {
            url = uri(rootProject.projectDir.resolve("libs/maven"))
        }
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
        maven {
            url = uri(rootProject.projectDir.resolve("libs/maven"))
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "GroupA"

include(":app")
include(":core")
include(":core-ui")
include(":data")
include(":domain")
include(":presentation")
