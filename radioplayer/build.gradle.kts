@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }

    android {
        namespace = "dev.markturnip.radioplayer"
        compileSdk = 34
        minSdk = 24

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }
    
    val xcf = XCFramework()
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "radioplayer"
            xcf.add(this)
            // Dynamic framework required for SwiftPM import integration
            isStatic = false
        }
    }

    swiftPMDependencies {
        iosMinimumDeploymentTarget.set("14.0")
        localSwiftPackage(
            directory = layout.projectDirectory.dir("native"),
            products = listOf("RadioPlayer")
        )
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.media3.common)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.exoplayer.hls)
            implementation(libs.androidx.media3.session)
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
