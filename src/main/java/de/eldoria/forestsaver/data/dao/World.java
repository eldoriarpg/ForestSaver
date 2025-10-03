package de.eldoria.forestsaver.data.dao;

import de.eldoria.forestsaver.data.Nodes;
import org.bukkit.Location;

import java.util.Optional;
import java.util.UUID;

public class World {
    private final Nodes nodes;
    private UUID worldUid;

    public World(Nodes nodes, UUID worldUid) {
        this.nodes = nodes;
        this.worldUid = worldUid;
    }

    /**
     * Returns the node at the given location.
     * @param location location to get the node for
     * @return node at the given location, or empty if no node exists at the given location
     */
    public Optional<Node> getNode(Location location) {
        if (worldUid != location.getWorld().getUID()) {
            throw new IllegalArgumentException("Worlds don't match");
        }
        return nodes.getNode(location);
    }

    /**
     * Creates a new node in this world.
     * @return new node
     */
    public Node createNode() {
        return nodes.createNode(worldUid);
    }
}
