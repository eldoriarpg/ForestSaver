package de.eldoria.forestsaver.configuration.elements;

import org.bukkit.Tag;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Presets {
    private Map<String, Preset> presets = Map.of("default", new Preset(Set.of(Tag.LOGS, Tag.LEAVES), Collections.emptySet()));

    public Optional<Preset> getPreset(String name) {
        return Optional.ofNullable(presets.get(name));
    }
}
