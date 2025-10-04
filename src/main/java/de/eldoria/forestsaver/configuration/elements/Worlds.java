package de.eldoria.forestsaver.configuration.elements;

import de.eldoria.forestsaver.configuration.Configuration;
import dev.chojo.ocular.Configurations;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Worlds {
    Map<UUID, Map<ResourceType, String>> worldPresets = new HashMap<>();
    Map<ResourceType, String> defaultPreset = null;

    /**
     * Sets the default preset for the given world.
     *
     * @param world  world to set the default preset for
     * @param preset preset to set as default preset, or null to remove the preset
     */
    public void setDefaultPreset(World world, ResourceType type, @Nullable Preset preset) {
        if (preset == null) {
            worldPresets.computeIfAbsent(world.getUID(), k -> new HashMap<>()).put(type, null);
        } else {
            worldPresets.computeIfAbsent(world.getUID(), k -> new HashMap<>()).put(type, preset.name());
        }
    }

    /**
     * Returns the preset for the given world.
     *
     * @param world world to get the preset for
     * @return Optional of preset name, or empty optional if no preset is set
     */
    public Optional<String> presetFor(World world, ResourceType type) {
        return Optional.ofNullable(worldPresets.computeIfAbsent(world.getUID(), k -> Collections.emptyMap()).get(type)).or(() -> Optional.ofNullable(defaultPreset.get(type)));
    }

    public void bootstrap(Plugin plugin, Configurations<Configuration> configuration) {
        Presets presets = configuration.secondary(Presets.KEY);
        if (defaultPreset == null) defaultPreset = new HashMap<>();
        if (worldPresets == null) worldPresets = new HashMap<>();
        for (ResourceType type : ResourceType.values()) {
            Optional<Preset> preset = presets.getPreset(defaultPreset.computeIfAbsent(type, k -> null), type);
            if (preset.isEmpty() && defaultPreset.get(type) != null) {
                plugin.getLogger().warning("Default preset %s not found for type %s, removing".formatted(defaultPreset.get(type), type));
                defaultPreset.put(type, null);
            }
        }

        for (UUID uuid : new HashSet<>(worldPresets.keySet())) {
            World world = plugin.getServer().getWorld(uuid);
            if (world == null) {
                plugin.getLogger().warning("World " + uuid + " not found, removing from default presets");
                worldPresets.remove(uuid);
                continue;
            }

            for (ResourceType type : ResourceType.values()) {
                if (presets.getPreset(worldPresets.computeIfAbsent(uuid, k -> new HashMap<>()).computeIfAbsent(type, k -> null), type).isEmpty() &&  worldPresets.get(uuid).get(type) != null) {
                    plugin.getLogger().warning("Default preset for world %s (%s) not found, removing".formatted(world.getName(), worldPresets.get(uuid).get(type)));
                    worldPresets.remove(uuid).put(type, null);
                }
            }
        }
        plugin.getServer().getWorlds().forEach(world -> {
            HashMap<ResourceType, String> map = new HashMap<>();
            Arrays.stream(ResourceType.values()).forEach(type -> map.put(type, null));
            worldPresets.putIfAbsent(world.getUID(), map);
        });
    }
}
