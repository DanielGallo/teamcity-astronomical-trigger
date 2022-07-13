import com.github.jk1.license.render.JsonReportRenderer

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("com.github.rodm.teamcity-server") version "1.4.1"
    id("com.github.rodm.teamcity-environments") version "1.4.1"
    id("com.github.jk1.dependency-license-report") version "1.17"
}

group = "org.jetbrains.teamcity"
val pluginVersion = project.findProperty("PluginVersion") ?: "999999-snapshot"
version = pluginVersion

val teamcityVersion = "2022.1-SNAPSHOT"
val ktorVersion = "2.0.0"

extra["teamcityVersion"] = teamcityVersion
extra["downloadsDir"] = project.findProperty("downloads.dir") ?: "${rootDir}/downloads"

repositories {
    maven(url="https://cache-redirector.jetbrains.com/maven-central")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("io.ktor:ktor-client-core:${ktorVersion}")
    implementation("io.ktor:ktor-client-cio:${ktorVersion}")
    implementation("io.ktor:ktor-client-content-negotiation:${ktorVersion}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.2")
    implementation("com.github.salomonbrys.kotson:kotson:2.5.0")
    implementation("com.github.ben-manes.caffeine:caffeine:2.9.2")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    provided("org.jetbrains.teamcity:server-api:${teamcityVersion}")
    provided("org.jetbrains.teamcity:oauth:${teamcityVersion}")
    provided("org.jetbrains.teamcity:server-web-api:${teamcityVersion}")
    provided("org.jetbrains.teamcity.internal:server:${teamcityVersion}")
    provided("org.jetbrains.teamcity.internal:web:${teamcityVersion}")

    testImplementation(kotlin("test"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}

tasks.test {
    useJUnitPlatform()
}

teamcity {
    version = "2021.2"

    server {
        archiveName = "astronomical-trigger.zip"
        descriptor = file("teamcity-plugin.xml")
        tokens = mapOf("Version" to pluginVersion)

        files {
            into("kotlin-dsl") {
                from("src/kotlin-dsl")
            }
        }
    }
    environments {
        baseHomeDir = "teamcity/servers"
        baseDataDir = "teamcity/data"

        register("teamcity2021.2") {
            version = "2021.2.3"
            javaHome = "/Library/Java/JavaVirtualMachines/amazon-corretto-11.jdk/Contents/Home"
            serverOptions("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5010")
        }
    }
}

licenseReport {
    renderers = arrayOf(JsonReportRenderer("third-party-libs.json"))
}