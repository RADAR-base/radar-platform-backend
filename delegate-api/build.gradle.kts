plugins {
    application
}

application {
    mainClass.set("org.radarbase.delegate.DelegateApplication")
}

dependencies {
    implementation(project(":contract"))
    implementation(project(":core"))
    testImplementation(libs.mockk)
}
