dependencies {
    implementation(project(":contract"))
    implementation("org.radarbase:radar-jersey:0.12.0")

    testImplementation("io.mockk:mockk:1.14.2")
}

plugins {
    application
}

application {
    mainClass.set("org.radarbase.delegate.DelegateApplication")
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
