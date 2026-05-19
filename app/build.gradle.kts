import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.URI
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.dokka")
}

private val hiltVersion = "2.58"
private val ktorVersion = "3.5.0"

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
                ?.let { storeFile = file(it) }
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
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.core:core-ktx:1.18.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-gson:$ktorVersion")

    val composeBom = platform("androidx.compose:compose-bom:2026.05.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")
}

dokka {
    dokkaPublications {
        html {
            moduleName.set("Connection Info")
            outputDirectory.set(layout.buildDirectory.dir("documentation/html"))
        }
    }
    dokkaSourceSets {
        named("main") {
            reportUndocumented.set(false)
            skipEmptyPackages.set(true)
            suppressGeneratedFiles.set(true)
            sourceLink {
                localDirectory.set(file("src/main/java"))
                remoteUrl.set(URI("https://github.com/KaBoomSoftware/Connection-Info/tree/main/app/src/main/java"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}

tasks.register("buildProjectDocumentation") {
    group = "documentation"
    description = "Builds the project API documentation as Dokka HTML."
    dependsOn(tasks.named("dokkaGeneratePublicationHtml"))
}
