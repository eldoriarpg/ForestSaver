package de.eldoria.forestsaver.data.dao;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.converter.StandardValueConverter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BlockVector;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.UUID_STRING;

public record Fragment(long nodeId, UUID world, BlockVector position, String data, boolean alwaysRestorable,
                       Instant destroyed) {

    public static Fragment fromBlock(long id, Block block, boolean alwaysRestorable) {
        var data = block.getBlockData().getAsString();
        block.getLocation();
        return new Fragment(id, block.getWorld().getUID(), block.getLocation().toVector().toBlockVector(), data, alwaysRestorable, null);
    }

    @MappingProvider({"node_id", "world", "x", "y", "z", "block_data", "always_restore", "destroyed"})
    public static RowMapping<Fragment> map() {
        return row -> new Fragment(row.getLong("node_id"),
                row.get("world", UUID_STRING),
                new BlockVector(row.getInt("x"),
                        row.getInt("y"),
                        row.getInt("z")),
                row.getString("block_data"),
                row.getBoolean("always_restore"),
                row.get("destroyed", StandardValueConverter.INSTANT_TIMESTAMP));
    }

    public void restore(World world) {
        // TODO: logging
        CompletableFuture.runAsync(() -> query("""
                UPDATE fragments SET destroyed = NULL WHERE node_id = :id AND x = :x AND y = :y AND z = :z;
                UPDATE nodes SET last_modified = now() WHERE id = :id;""")
                .single(call().bind("id", nodeId).bind("x", position.getBlockX()).bind("y", position.getBlockY()).bind("z", position.getBlockZ()))
                .update());
        world.setBlockData(position.toLocation(world), blockData());
    }

    public BlockData blockData() {
        return Bukkit.createBlockData(data);
    }
}
