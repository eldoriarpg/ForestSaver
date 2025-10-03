plugins {
    id("java")
    alias(libs.plugins.shadow)
    alias(libs.plugins.spotless)
    alias(libs.plugins.pluginyml)
    alias(libs.plugins.runserver)
    alias(libs.plugins.publishdata)
}

group = "de.eldoria"
version = "1.0.0"

repositories {
    maven("https://eldonexus.de/repository/maven-public/")
    maven("https://eldonexus.de/repository/maven-proxies/")
}

publishData {
    useEldoNexusRepos()
}

dependencies {
    paperLibrary(libs.bundles.utilities)
    paperLibrary(libs.bundles.sadu)
    paperLibrary(libs.bundles.database)
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly(libs.worldguard)
    implementation(libs.ocular)
    implementation(libs.jackson.yaml)
    implementation("de.eldoria.jacksonbukkit", "paper", "1.3.0")

    paperLibrary("org.incendo:cloud-annotations:2.0.0")
    annotationProcessor("org.incendo:cloud-annotations:2.0.0")
    paperLibrary("org.incendo:cloud-paper:2.0.0-beta.10")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

}

tasks {
    runServer {
        minecraftVersion("1.21.8")
        downloadPlugins {
            url("https://ci.athion.net/job/FastAsyncWorldEdit/1175/artifact/artifacts/FastAsyncWorldEdit-Paper-2.13.3-SNAPSHOT-1175.jar")
            url("https://download.luckperms.net/1600/bukkit/loader/LuckPerms-Bukkit-5.5.14.jar")
            modrinth("worldguard", "7.0.14")
        }

        jvmArgs("-Dcom.mojang.eula.agree=true", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005")
    }

    test {
        useJUnitPlatform()
    }

    generatePaperPluginDescription {
        useDefaultCentralProxy()
    }

    shadowJar {
        relocate("com.fasterxml", "de.eldoria.forestsaver.libs.fasterxml")
    }
}

paper {
    main = "de.eldoria.forestsaver.ForestSaverPlugin"
    bootstrapper = "de.eldoria.forestsaver.ForestSaverBootstrapper"
    loader = "de.eldoria.forestsaver.ForestSaverLoader"
    apiVersion = "1.20.6"

    generateLibrariesJson = true

    serverDependencies {
        register("WorldGuard") {
            required = false
        }
    }
}


