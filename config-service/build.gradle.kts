plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.github.johnrengelman.shadow")
    kotlin("plugin.serialization")
    application
}

application {
    mainClass.set("org.radarbase.config.ConfigServiceApplication")
}

dependencies {
    // Jersey and Radar Jersey are provided from root build.gradle.kts via subprojects block

    // Ktor Client for GitHub provider
    implementation("io.ktor:ktor-client-core:2.3.5")
    implementation("io.ktor:ktor-client-cio:2.3.5")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
