package de.eldoria.forestsaver.configuration.elements.presets.parts;

import org.bukkit.Material;

import java.util.Collection;

public interface MaterialProvider {
    Collection<Material> materials();
}
