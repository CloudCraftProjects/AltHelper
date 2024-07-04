plugins {
    id("java-library")
    id("maven-publish")

    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.3.0"
}

group = "dev.booky"
version = "3.0.0"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
}

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.lowercase()
        from(components["java"])
    }
}

bukkit {
    main = "$group.alts.AltHelperMain"
    apiVersion = "1.20"
    authors = listOf("booky10")
    commands.register("althelper") {
        permission = "althelper.use"
        aliases = listOf("alts")
        usage = "Usage: /<command> [all|<player>|<ip address>]"
    }
}

tasks {
    runServer {
        minecraftVersion("1.20.6")
    }

    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
    }

    withType<Jar> {
        // no spigot mappings are used, disable useless remapping step
        manifest.attributes("paperweight-mappings-namespace" to "mojang")
    }
}
