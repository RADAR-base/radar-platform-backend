plugins {
    application
}

application {
    mainClass.set("org.radarbase.project.ProjectServiceApplication")
}

dependencies {
    implementation(project(":core"))
}
