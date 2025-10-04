package de.eldoria.forestsaver.data.dao;

import de.eldoria.forestsaver.configuration.elements.ResourceType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BlockVector;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.UUID_STRING;

public class Fragment {
    protected final ResourceType type;
    protected final UUID world;
    protected final BlockVector position;
    protected final String data;
    protected final boolean alwaysRestorable;
    protected final Instant destroyed;
    protected final int token;


    public Fragment(ResourceType type, UUID world, BlockVector position, String data, boolean alwaysRestorable, Instant destroyed, int token) {
        this.type = type;
        this.world = world;
        this.position = position;
        this.data = data;
        this.alwaysRestorable = alwaysRestorable;
        this.destroyed = destroyed;
        this.token = token;
    }

    /**
     * Restores the block.
     *
     * @param world world to restore the block in
     */
    public void restore(World world) {
        // TODO: logging
        // TODO: Maybe sync back to main thread?
        CompletableFuture.runAsync(() -> query("""
                UPDATE fragments SET destroyed = NULL, token = NULL WHERE world = :world::UUID AND x = :x AND y = :y AND z = :z;""")
                .single(call().bind("world", this.world, UUID_STRING).bind("x", position.getBlockX()).bind("y", position.getBlockY()).bind("z", position.getBlockZ()))
                .update());
        world.setBlockData(position.toLocation(world), blockData());
    }

    /**
     * Returns the block data.
     *
     * @return block data
     */
    public BlockData blockData() {
        return Bukkit.createBlockData(data);
    }

    public ResourceType type() {
        return type;
    }

    public UUID world() {
        return world;
    }

    public BlockVector position() {
        return position;
    }

    public String data() {
        return data;
    }

    public boolean alwaysRestorable() {
        return alwaysRestorable;
    }

    public Instant destroyed() {
        return destroyed;
    }

    public int token() {
        return token;
    }
}
