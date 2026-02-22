enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        maven("https://packages.jetbrains.team/maven/p/kt/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://packages.jetbrains.team/maven/p/kt/dev")
        google()
        mavenCentral()
    }
}

rootProject.name = "radioplayer-kt"
include(":radioplayer")