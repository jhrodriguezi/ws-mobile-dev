import java.util.Properties

// Load the properties file
val localPropertiesFile = rootDir.resolve("local.properties")
val properties = Properties()

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { properties.load(it) }
}

// Access your variable
val myApiKey = properties.getProperty("MAPBOX_DOWNLOADS_TOKEN") ?: "default_value"

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

        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                password = myApiKey
            }
        }
    }
}

rootProject.name = "Tic Tac Toe"
include(":app")
 