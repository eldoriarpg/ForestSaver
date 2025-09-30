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
    bukkitLibrary(libs.bundles.utilities)
    bukkitLibrary(libs.bundles.sadu)
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    runServer {
        minecraftVersion("1.21.8")
        downloadPlugins {
            url("https://ci.athion.net/job/FastAsyncWorldEdit/1175/artifact/artifacts/FastAsyncWorldEdit-Paper-2.13.3-SNAPSHOT-1175.jar")
            url("https://download.luckperms.net/1600/bukkit/loader/LuckPerms-Bukkit-5.5.14.jar")
            modrinth("worldguard", "7.0.14")
        }

        jvmArgs("-Dcom.mojang.eula.agree=true")
    }

    test {
        useJUnitPlatform()
    }
}

bukkit {
    main = "de.eldoria.forestsaver.ForestSaverPlugin"
}


