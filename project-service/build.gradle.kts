plugins {
    application
}

application {
    mainClass.set("org.radarbase.project.ProjectServiceApplication")
}

dependencies {
    implementation(project(":core"))
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
