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
