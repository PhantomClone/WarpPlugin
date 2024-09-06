import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
    id("dev.s7a.gradle.minecraft.server") version "3.0.0"
}

group = "me.phantomclone.warpplugin"
version = "1.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation("de.chojo.sadu", "sadu", "1.3.1")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.flywaydb:flyway-core:9.22.3")

    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testImplementation("org.mockito:mockito-core:5.13.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.13.0")
    testImplementation("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
}

tasks.withType<JavaCompile> {
    options.release.set(21)
    options.encoding = "UTF-8"
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
}



tasks.named<Test>("test") {
    useJUnitPlatform()

    maxHeapSize = "1G"

    testLogging {
        events("passed")
    }
}

task<LaunchMinecraftServerTask>("testPlugin") {
    dependsOn("shadowJar")

    doFirst {
        copy {
            from(layout.buildDirectory.dir("libs/${project.name}-${project.version}.jar"))
            into(layout.buildDirectory.dir("MinecraftServer/plugins"))
        }
    }

    jarUrl.set(LaunchMinecraftServerTask.JarUrl.Paper("1.21.1"))
    agreeEula.set(true)
}