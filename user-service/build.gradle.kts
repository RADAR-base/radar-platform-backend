plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.github.johnrengelman.shadow")
    kotlin("plugin.serialization")
    application
}

application {
    mainClass.set("org.radarbase.user.UserApplication")
}

dependencies {
    // Jersey dependencies
    implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-http:3.1.2")
    implementation("org.glassfish.jersey.inject:jersey-hk2:3.1.2")
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson:3.1.2")
    implementation("org.glassfish.jersey.core:jersey-server:3.1.2")

    // Radar Jersey
    implementation("org.radarbase:radar-jersey:0.8.0")

    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
    testImplementation("io.ktor:ktor-client-mock:2.3.5")
    testImplementation("io.mockk:mockk:1.14.2")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(mapOf("Main-Class" to application.mainClass.get()))
    }
}

tasks.shadowJar {
    archiveBaseName.set("app")
    archiveVersion.set("")
    archiveClassifier.set("")
    mergeServiceFiles()
    manifest {
        attributes(mapOf("Main-Class" to application.mainClass.get()))
    }
}
