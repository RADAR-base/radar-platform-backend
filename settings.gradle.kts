rootProject.name = "radar-platform-backend"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
            mavenContent { snapshotsOnly() }
        }
        maven { url = uri("https://jitpack.io") }
    }
}

rootDir.listFiles()
    ?.also {
        println("File is: $it")
    }
    ?.filter { it.isDirectory }
    ?.filter { File(it, "build.gradle.kts").exists() || File(it, "build.gradle").exists() }
    ?.forEach { dir ->
        val modulePath = ":${dir.name}"
        println("Adding module: $modulePath")
        include(modulePath)
        project(modulePath).projectDir = dir
        println("Module added with directory ${dir.absolutePath}")
    }
