package de.eldoria.forestsaver.configuration;

import de.eldoria.forestsaver.configuration.elements.Database;
import de.eldoria.forestsaver.configuration.elements.Presets;
import de.eldoria.forestsaver.configuration.elements.Restore;

public class Configuration {
    Database database = new Database();
    Presets presets = new Presets();
    Restore restore = new Restore();

    public Database database() {
        return database;
    }

    public Presets presets() {
        return presets;
    }

    public Restore restore() {
        return restore;
    }
}
