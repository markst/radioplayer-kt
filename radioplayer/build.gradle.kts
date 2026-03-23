import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    androidTarget {

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
        iosDeploymentVersion.set("14.0")
        localPackage(
            directory = layout.projectDirectory.dir("native"),
            products = listOf("RadioPlayer")
        )
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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(
        groupId = "io.github.markst",
        artifactId = "radioplayer",
        version = project.findProperty("VERSION_NAME") as String? ?: "0.1.0"
    )

    pom {
        name = "RadioPlayer"
        description = "Kotlin Multiplatform radio player library with ExoPlayer (Android) and AVPlayer (iOS) backends."
        inceptionYear = "2024"
        url = "https://github.com/markst/radioplayer-kt"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "markst"
                name = "Mark Turriff"
                url = "https://github.com/markst"
            }
        }
        scm {
            url = "https://github.com/markst/radioplayer-kt"
            connection = "scm:git:git://github.com/markst/radioplayer-kt.git"
            developerConnection = "scm:git:ssh://git@github.com/markst/radioplayer-kt.git"
        }
    }
}
