plugins {
    application
}

application {
    mainClass.set("org.radarbase.config.ConfigServiceApplication")
}

dependencies {
    implementation(project(":core"))
}
