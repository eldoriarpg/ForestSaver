package de.eldoria.forestsaver.data.data;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.converter.StandardValueConverter;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

import java.time.Instant;
import java.util.UUID;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.UUID_BYTES;

public record Fragment(long nodeId, UUID world, BlockVector position, BlockData data, Instant destroyed) {

    public static Fragment fromBlock(long id, Block block) {
        BlockData data = BlockData.fromBlock(block);
        block.getLocation();
        return new Fragment(id, block.getWorld().getUID(), block.getLocation().toVector().toBlockVector(), data, null);
    }

    @MappingProvider({"node_id", "world", "x", "y", "z", "material", "block_data", "destroyed"})
    public static RowMapping<Fragment> map() {
        return row -> new Fragment(row.getLong("node_id"),
                row.get("world", UUID_BYTES),
                new BlockVector(row.getInt("x"),
                        row.getInt("y"),
                        row.getInt("z")),
                new BlockData(
                        row.getEnum("material", Material.class),
                        row.getString("block_data")),
                row.get("destroyed", StandardValueConverter.INSTANT_TIMESTAMP));
    }

    public void restore(World world) {
        query("UPDATE fragments SET destroyed = NULL WHERE node_id = :id AND x = :x AND y = :y AND z = :z")
                .single(call().bind("id", nodeId).bind("x", position.getBlockX()).bind("y", position.getBlockX()).bind("z", position.getBlockZ()))
                .update();
        world.setType(position.toLocation(world), data.material());
        world.setBlockData(position.toLocation(world), data.createData());
    }
}
