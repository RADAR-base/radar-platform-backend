rootProject.name = "radar-platform-backend"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include(":config-service")
include(":contract")
include(":core")
include(":data-sources-service")
include(":delegate-api")
include(":gateway-service")
include(":project-service")
include(":user-service")
