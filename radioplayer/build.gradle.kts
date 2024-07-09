import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import co.touchlab.skie.configuration.EnumInterop
import co.touchlab.skie.configuration.SealedInterop

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("co.touchlab.skie") version "0.8.2"
}

skie {
    features {
        group("co.touchlab.skie.types") {
            SealedInterop.Enabled(false)
            EnumInterop.Enabled(false)
        }
    }
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    val xcf = XCFramework()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "radioplayer"
            xcf.add(this)
            isStatic = true
        }
        it.compilations {
            val main by getting {
                val myInterop by cinterops.creating {
                    definitionFile.set(project.file("radioplayer.def"))
                }
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.media3.common)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.exoplayer.hls)
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            // TODO: Figure out passing `Context` to `ExoPlayer` without koin:
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "dev.markturnip.radioplayer"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
dependencies {
    implementation(libs.androidx.media3.common)
}
