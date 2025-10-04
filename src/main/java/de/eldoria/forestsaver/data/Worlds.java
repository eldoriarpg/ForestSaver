package de.eldoria.forestsaver.data;

import de.eldoria.forestsaver.data.dao.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Worlds {
    private final Nodes nodes;
    private final Fragments fragments;
    public Map<UUID, World> worlds = new HashMap<>();

    public Worlds(Nodes nodes, Fragments fragments) {
        this.nodes = nodes;
        this.fragments = fragments;
    }

    /**
     * Returns the world with the given UUID.
     *
     * @param uuid UUID of the world to get
     * @return world with the given UUID
     */
    public World getWorld(UUID uuid) {
        return worlds.computeIfAbsent(uuid, (k) -> new World(nodes,fragments, uuid));
    }
}
