package de.eldoria.forestsaver.data.dao;

import de.eldoria.forestsaver.configuration.elements.Preset;
import de.eldoria.forestsaver.configuration.elements.ResourceType;
import de.eldoria.forestsaver.data.Fragments;
import de.eldoria.forestsaver.data.Nodes;
import org.bukkit.Location;
import org.bukkit.block.BlockState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class World {
    private final Nodes nodes;
    private final Fragments fragments;
    private final UUID worldUid;

    public World(Nodes nodes, Fragments fragments, UUID worldUid) {
        this.nodes = nodes;
        this.fragments = fragments;
        this.worldUid = worldUid;
    }

    /**
     * Returns the node at the given location.
     *
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
     *
     * @return new node
     */
    public Node createNode() {
        return nodes.createNode(worldUid);
    }

    /**
     * Marks the given block as destroyed.
     *
     * @param block block to mark as destroyed
     */
    public void breakBlock(ResourceType resourceType, Preset preset, BlockState block) {
        fragments.breakBlock(worldUid, resourceType, preset, block);
    }

    /**
     * Returns all idle nodes of the given world.
     *
     * @return list of idle nodes
     */
    public List<Node> idleNodes() {
        return nodes.idleNodes(worldUid);
    }

    /**
     * Returns all idle nodes of the given world.
     *
     * @return list of idle nodes
     */
    public List<Fragment> idleFragments(ResourceType type) {
        return fragments.idleFragments(worldUid, type);
    }


}
