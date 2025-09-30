package de.eldoria.forestsaver.data;

import de.eldoria.forestsaver.data.data.World;

import java.util.Map;
import java.util.UUID;

public class Worlds {
    public Map<UUID, World> worlds;

    public Worlds() {
    }

    public World getWorld(UUID uuid) {
        return worlds.computeIfAbsent(uuid, (k) -> new World());
    }
}
