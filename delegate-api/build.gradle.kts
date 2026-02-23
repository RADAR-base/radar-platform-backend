dependencies {
    implementation(project(":user-service"))
    implementation(project(":data-sources-service"))
    implementation(project(":project-service"))
    implementation("org.radarbase:radar-jersey:0.12.0")
}

plugins {
    application
}

application {
    mainClass.set("org.radarbase.delegate.DelegateApplication")
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
