plugins {
    application
}

application {
    mainClass.set("org.radarbase.gateway.GatewayServiceApplication")
}

dependencies {
    implementation(project(":core"))
    implementation(libs.jersey.media.multipart)
}
