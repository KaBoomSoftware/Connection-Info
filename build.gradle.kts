plugins {
    id("com.android.application") version "9.2.1" apply false
    id("com.android.kotlin.multiplatform.library") version "9.2.1" apply false
    id("org.jetbrains.kotlin.multiplatform") version "2.3.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.21" apply false
    id("org.jetbrains.compose") version "1.11.0" apply false
    id("com.google.devtools.ksp") version "2.3.2" apply false
    id("com.google.dagger.hilt.android") version "2.59.2" apply false
    id("org.jetbrains.dokka") version "2.2.0" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
