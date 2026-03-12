plugins {
    alias(libs.plugins.radar.kotlin) apply false
    alias(libs.plugins.radar.dependency.management)
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.shadow) apply false
}

val projectVersion = libs.versions.project.get()

val rootImplDeps = listOf(
    libs.jersey.grizzly,
    libs.jersey.hk2,
    libs.jersey.jackson,
    libs.radar.jersey,
    libs.kotlinx.serialization.json,
    libs.ktor.client.core,
    libs.ktor.client.cio,
    libs.ktor.client.contentNegotiation,
    libs.ktor.serialization.json,
    libs.kotlinx.coroutines.core,
    libs.logback.classic,
)

val rootTestDeps = listOf(
    libs.jersey.test.core,
    libs.jersey.test.grizzly,
)

allprojects {
    group = "org.radarbase"
    version = projectVersion
}

subprojects {
    apply(plugin = "org.radarbase.radar-kotlin")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    repositories {
        maven { url = uri("https://jitpack.io") }
    }

    dependencies {
        rootImplDeps.forEach { "implementation"(it) }
        rootTestDeps.forEach { "testImplementation"(it) }
    }

    tasks.withType<Test> {
        enabled = false
    }
}
