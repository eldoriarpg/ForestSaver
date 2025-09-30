rootProject.name = "forest-saver"


pluginManagement{
    repositories{
        mavenLocal()
        gradlePluginPortal()
        maven{
            name = "EldoNexus"
            url = uri("https://eldonexus.de/repository/maven-public/")
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("sadu", "2.3.3")

            library("sadu-core", "de.chojo.sadu", "sadu-core").versionRef("sadu")
            library("sadu-queries", "de.chojo.sadu", "sadu-queries").versionRef("sadu")
            library("sadu-datasource", "de.chojo.sadu", "sadu-datasource").versionRef("sadu")
            library("sadu-updater", "de.chojo.sadu", "sadu-updater").versionRef("sadu")
            library("sadu-postgresql", "de.chojo.sadu", "sadu-postgresql").versionRef("sadu")
            library("sadu-mariadb", "de.chojo.sadu", "sadu-mariadb").versionRef("sadu")
            library("sadu-mysql", "de.chojo.sadu", "sadu-mysql").versionRef("sadu")
            bundle(
                "sadu",
                listOf(
                    "sadu-core",
                    "sadu-queries",
                    "sadu-datasource",
                    "sadu-updater",
                    "sadu-postgresql",
                    "sadu-mariadb",
                    "sadu-mysql"
                )
            )

            version("utilities", "2.1.11")
            library("eldoutil-plugin", "de.eldoria.util", "plugin").versionRef("utilities")
            library("eldoutil-jackson", "de.eldoria.util", "jackson-configuration").versionRef("utilities")
            library("eldoutil-serialization", "de.eldoria.util", "legacy-serialization").versionRef("utilities")
            library("eldoutil-metrics", "de.eldoria.util", "metrics").versionRef("utilities")
            library("eldoutil-updater", "de.eldoria.util", "updater").versionRef("utilities")
            library("eldoutil-crossversion", "de.eldoria.util", "crossversion").versionRef("utilities")
            bundle(
                "utilities",
                listOf(
                    "eldoutil-jackson",
                    "eldoutil-plugin",
                    "eldoutil-serialization",
                    "eldoutil-metrics",
                    "eldoutil-updater",
                    "eldoutil-crossversion"
                )
            )

            library("worldguard", "com.sk89q.worldguard", "worldguard-bukkit").version("7.0.14")

            plugin("publishdata", "de.chojo.publishdata").version("1.4.0")
            plugin("spotless", "com.diffplug.spotless").version("7.2.1")
            plugin("shadow", "com.gradleup.shadow").version("9.0.2")
            plugin("pluginyml", "net.minecrell.plugin-yml.bukkit").version("0.6.0")
            plugin("runserver", "xyz.jpenilla.run-paper").version("2.3.1")
        }
    }
}
