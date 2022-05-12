plugins {
    id("java-library")
    id("maven-publish")
    id("xyz.jpenilla.run-paper") version "1.0.6"
}

group = "dev.booky"
version = "3.0.0"

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    api("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
}

java {
    withSourcesJar()

    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.toLowerCase()
        from(components["java"])
    }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    runServer {
        minecraftVersion.set("1.18.2")
    }
}
