package de.eldoria.forestsaver.configuration;

import de.eldoria.forestsaver.configuration.elements.Database;
import de.eldoria.forestsaver.configuration.elements.Nodes;
import de.eldoria.forestsaver.configuration.elements.Presets;
import de.eldoria.forestsaver.configuration.elements.Restore;
import de.eldoria.forestsaver.configuration.elements.Worlds;
import dev.chojo.ocular.Configurations;
import org.bukkit.plugin.Plugin;

public class Configuration {
    Database database = new Database();
    Restore restore = new Restore();
    Nodes nodes = new Nodes();
    Worlds worlds = new Worlds();

    public Database database() {
        return database;
    }

    public Restore restore() {
        return restore;
    }

    public Nodes nodes() {
        return nodes;
    }

    public Worlds worlds() {
        return worlds;
    }

    public void bootstrap(Plugin plugin, Configurations<Configuration> configuration) {
        worlds.bootstrap(plugin, configuration);
    }

}
