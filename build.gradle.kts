buildscript {
    dependencies.constraints {
        "classpath"("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0-titan-218!!")
    }
}

plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
}
