package de.eldoria.forestsaver.configuration.elements;

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
    private Map<String, Preset> presets = null;
    private List<Preset> presetList = List.of(new Preset("default", Set.of(Tag.LOGS, Tag.LEAVES), Collections.emptySet()));

    public Optional<Preset> getPreset(String name) {
        if (presets == null) {
            presets = presetList.stream().collect(Collectors.toMap(Preset::name, e -> e, (e1, e2) -> e1, HashMap::new));
        }
        return Optional.ofNullable(presets.get(name));
    }

    public Collection<String> names() {
        return presets.keySet();
    }
}
