plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    )

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(projects.radioplayer)
        }
    }
}
