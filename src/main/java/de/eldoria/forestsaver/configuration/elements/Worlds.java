package de.eldoria.forestsaver.configuration.elements;

import de.eldoria.forestsaver.configuration.Configuration;
import dev.chojo.ocular.Configurations;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Worlds {
    Map<UUID, String> defaultPresets = new HashMap<>();
    String defaultPreset = null;

    /**
     * Sets the default preset for the given world.
     *
     * @param world  world to set the default preset for
     * @param preset preset to set as default preset, or null to remove the preset
     */
    public void setDefaultPreset(World world, @Nullable Preset preset) {
        if (preset == null) {
            defaultPresets.put(world.getUID(), null);
        } else {
            defaultPresets.put(world.getUID(), preset.name());
        }
    }

    /**
     * Returns the preset for the given world.
     *
     * @param world world to get the preset for
     * @return Optional of preset name, or empty optional if no preset is set
     */
    public Optional<String> presetFor(World world) {
        return Optional.ofNullable(defaultPresets.computeIfAbsent(world.getUID(), k -> null)).or(() -> Optional.ofNullable(defaultPreset));
    }

    public void bootstrap(Plugin plugin, Configurations<Configuration> configuration) {
        Presets presets = configuration.main().presets();

        if (defaultPreset != null) {
            Optional<Preset> preset = presets.getPreset(defaultPreset);
            if (preset.isEmpty()) {
                plugin.getLogger().warning("Default preset %s not found, removing".formatted(defaultPreset));
                defaultPreset = null;
            }
        }

        for (UUID uuid : new HashSet<>(defaultPresets.keySet())) {
            if (presets.getPreset(defaultPresets.get(uuid)).isEmpty()) {
                defaultPresets.remove(uuid);
                World world = plugin.getServer().getWorld(uuid);
                if (world == null) {
                    plugin.getLogger().warning("World " + uuid + " not found, removing from default presets");
                    defaultPresets.remove(uuid);
                    continue;
                }
                plugin.getLogger().warning("Default preset for world %s (%s) not found, removing".formatted(world.getName(), defaultPresets.get(uuid)));
                defaultPresets.remove(uuid);
            }
        }
        plugin.getServer().getWorlds().forEach(world -> {
            defaultPresets.putIfAbsent(world.getUID(), null);
        });
    }
}
