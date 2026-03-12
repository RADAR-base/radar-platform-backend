plugins {
    application
}

application {
    mainClass.set("org.radarbase.datasources.DataSourcesApplication")
}

dependencies {
    implementation(project(":core"))
    testImplementation(libs.mockk)
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
