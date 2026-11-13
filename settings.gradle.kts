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

rootProject.name = "XFaceID"

// Fase 2: :app, :core:camera, :core:ai.
// Modul lain (core:auth, core:security, core:database, ui:standard,
// ui:lite, feature:xpassword) sudah disiapkan foldernya dan baru di-include
// saat fasenya tiba.
include(":app")
include(":core:camera")
include(":core:ai")
