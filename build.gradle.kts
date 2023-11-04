import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("dev.s7a.gradle.minecraft.server") version "3.0.0"
}

group = "me.phantomclone.warpplugin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation("de.chojo.sadu", "sadu", "1.3.1")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.flywaydb:flyway-core:9.22.3")

    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.16.10")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        relocate("de.chojo.sadu", "me.phantomclone.warpplugin.sadu")
        archiveFileName.set("${project.name}-${project.version}.jar")
        //minimize() TODO optimize
    }

    compileJava {
        options.release.set(17)
        options.encoding = "UTF-8"
    }
}

task<LaunchMinecraftServerTask>("testPlugin") {
    dependsOn("shadowJar")

    doFirst {
        copy {
            from(buildDir.resolve("libs/${project.name}-${project.version}.jar"))
            into(buildDir.resolve("MinecraftServer/plugins"))
        }
    }

    jarUrl.set(LaunchMinecraftServerTask.JarUrl.Paper("1.20.2"))
    agreeEula.set(true)
}