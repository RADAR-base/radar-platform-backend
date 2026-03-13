plugins {
    application
}

application {
    mainClass.set("org.radarbase.user.UserApplication")
}

dependencies {
    implementation(project(":core"))
    implementation(libs.jersey.server)

    testImplementation(libs.mockk)
    testImplementation(libs.ktor.client.mock)
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
