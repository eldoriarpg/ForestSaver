package de.eldoria.forestsaver.configuration.elements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.eldoria.forestsaver.ForestSaverPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Tag;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Presets {
    /**
     * A map representation of the preset list.
     * <p>
     * The map is created on demand.
     */
    @JsonIgnore
    private Map<String, Preset> presetMap = null;
    /**
     * A list of presets.
     */
    private List<Preset> presets = List.of(new Preset("default", Set.of(Tag.LOGS, Tag.LEAVES), Collections.emptySet()));

    /**
     * Returns the preset with the given name.
     * @param name Name of the preset.
     * @return Optional of preset, or empty optional if no preset with the given name exists.
     */
    public Optional<Preset> getPreset(String name) {
        if (presetMap == null) {
            presetMap = presets.stream().collect(Collectors.toMap(Preset::name, e -> e, (e1, e2) -> {
                ForestSaverPlugin.getInstance().getLogger().warning("Duplicate preset name: " + name);
                return e1;
            }, HashMap::new));
        }
        return Optional.ofNullable(presetMap.get(name));
    }

    /**
     * Returns a collection of all preset names.
     * @return Collection of preset names.
     */
    public Collection<String> names() {
        return presetMap.keySet();
    }
}
