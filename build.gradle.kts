plugins {
    id("java-library")
    id("maven-publish")

    alias(libs.plugins.pluginyml.bukkit)
    alias(libs.plugins.runtask.paper)
    alias(libs.plugins.shadow)
}

group = "dev.booky"
version = "3.0.0"

val plugin: Configuration by configurations.creating { isTransitive = false }

repositories {
    maven("https://repo.cloudcraftmc.de/public/")
}

dependencies {
    compileOnly(libs.paper.api)

    implementation(libs.bstats)
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
        pluginJars.from(plugin.resolve())
    }

    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
    }

    withType<Jar> {
        // no spigot mappings are used, disable useless remapping step
        manifest.attributes("paperweight-mappings-namespace" to "mojang")
    }

    shadowJar {
        relocate("org.bstats", "${project.group}.alts.bstats")
    }

    assemble {
        dependsOn(shadowJar)
    }
}
