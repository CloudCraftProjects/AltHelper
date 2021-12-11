plugins {
    `java-library`
    `maven-publish`
}

group = "tk.booky"
version = "2.4.1"

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    api("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.toLowerCase()
        from(components["java"])
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}
