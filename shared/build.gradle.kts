import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.mavenPublish)
    signing
}

group = providers.gradleProperty("GROUP").getOrElse("io.github.shahid-iqbal")
version = providers.gradleProperty("VERSION_NAME").getOrElse("0.0.1")

signing {
    useGpgCmd()
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    androidLibrary {
       namespace = "com.shahid.tech.qibla.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    pom {
        name.set("GeoQibla")
        description.set("A Kotlin Multiplatform Compose library for Qibla direction, location, compass state, and default UI.")
        inceptionYear.set("2026")
        url.set("https://github.com/shahid-iqbal/geoqibla")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("shahid-iqbal")
                name.set("Shahid Iqbal")
                url.set("https://github.com/shahid-iqbal")
            }
        }

        scm {
            url.set("https://github.com/shahid-iqbal/geoqibla")
            connection.set("scm:git:git://github.com/shahid-iqbal/geoqibla.git")
            developerConnection.set("scm:git:ssh://git@github.com/shahid-iqbal/geoqibla.git")
        }
    }
}
