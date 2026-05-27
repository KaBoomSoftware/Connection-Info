import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

private val secretsPropertiesFile = rootProject.file("keystore.properties")
private fun signingEnvironmentValue(name: String): String = System.getenv(name).orEmpty()

private val secretProperties = Properties().apply {
    if (secretsPropertiesFile.exists()) {
        load(FileInputStream(secretsPropertiesFile))
    } else {
        setProperty("releaseKeyAlias", signingEnvironmentValue("releaseKeyAlias"))
        setProperty("releaseKeyPassword", signingEnvironmentValue("releaseKeyPassword"))
        setProperty("releaseKeyStore", signingEnvironmentValue("releaseKeyStore"))
        setProperty("releaseStorePassword", signingEnvironmentValue("releaseStorePassword"))
    }
}

private val versionPropsFile = file("version.properties")
private val versionProps = Properties().apply {
    if (versionPropsFile.canRead()) {
        load(FileInputStream(versionPropsFile))
    } else {
        throw FileNotFoundException("Could not read version.properties!")
    }
}

private val majorVersion = versionProps["MAJOR_VERSION"].toString().toInt()
private val minorVersion = versionProps["MINOR_VERSION"].toString().toInt()
private val versionBuild = versionProps["VERSION_BUILD"].toString().toInt()
private val appVersionCode = majorVersion * 10_000 + minorVersion * 100 + versionBuild
private val versionString = "$majorVersion.$minorVersion.$versionBuild"

fun autoIncrementBuildNumber() {
    val releaseVersionProps = Properties()
    releaseVersionProps.load(FileInputStream(versionPropsFile))

    val nextBuild = releaseVersionProps["VERSION_BUILD"].toString().toInt() + 1
    releaseVersionProps["VERSION_BUILD"] = nextBuild.toString()
    releaseVersionProps.store(versionPropsFile.writer(), null)
}

android {
    namespace = "cz.kaboom.connectioninfo"
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        applicationId = "cz.kaboom.connectioninfo"
        minSdk = 23
        targetSdk = 36
        versionCode = appVersionCode
        versionName = versionString
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = secretProperties["releaseKeyAlias"].toString()
            keyPassword = secretProperties["releaseKeyPassword"].toString()
            secretProperties["releaseKeyStore"].toString()
                .takeIf(String::isNotBlank)
                ?.let { storeFile = rootProject.file(it) }
            storePassword = secretProperties["releaseStorePassword"].toString()
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

gradle.taskGraph.whenReady {
    if (hasTask(":app:assembleRelease")) {
        autoIncrementBuildNumber()
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.core:core-ktx:1.18.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    val composeBom = platform("androidx.compose:compose-bom:2026.05.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
