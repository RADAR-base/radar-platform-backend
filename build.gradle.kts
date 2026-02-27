plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.22" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.0.3"
    kotlin("plugin.serialization") version "1.9.22" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    java
}

allprojects {
    group = "org.radarbase"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-http:3.1.5")
        implementation("org.glassfish.jersey.inject:jersey-hk2:3.1.5")
        implementation("org.glassfish.jersey.media:jersey-media-json-jackson:3.1.5")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
        implementation("org.slf4j:slf4j-api:2.0.9")
        implementation("ch.qos.logback:logback-classic:1.4.14")
        implementation("org.radarbase:radar-jersey:0.12.1")

        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

        // Ktor Client
        implementation("io.ktor:ktor-client-core:2.3.5")
        implementation("io.ktor:ktor-client-cio:2.3.5")
        implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
        implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")

        // Ktor Server
        implementation("io.ktor:ktor-server-core:2.3.5")
        implementation("io.ktor:ktor-server-netty:2.3.5")
        implementation("io.ktor:ktor-server-content-negotiation:2.3.5")

        // Coroutines
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.7.3")

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.1")
        testImplementation("org.glassfish.jersey.test-framework:jersey-test-framework-core:3.1.5")
        testImplementation(
            "org.glassfish.jersey.test-framework.providers:jersey-test-framework-provider-grizzly2:3.1.5",
        )
        testImplementation("org.glassfish.jersey.media:jersey-media-json-jackson:3.1.5")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    ktlint {
        version.set("0.50.0")
    }
}
