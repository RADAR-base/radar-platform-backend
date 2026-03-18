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
