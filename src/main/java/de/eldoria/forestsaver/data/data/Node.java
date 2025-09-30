package de.eldoria.forestsaver.data.data;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.converter.StandardValueConverter;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public final class Node {
    private final long id;
    private final UUID world;
    private List<Fragment> fragments;

    public Node(long id, UUID world) {
        this.id = id;
        this.world = world;
    }

    public void addBlock(Block block) {
        if (fragments != null) {
            fragments.add(Fragment.fromBlock(id, block));
        }
    }

    public void addBlocks(List<BlockState> blocks) {
        query("INSERT INTO fragments (node_id, world, x, y, z, material, block_data) VALUES (:id, :world, :x, :y, :z, :material, :block_data)")
                .batch(blocks.stream()
                             .map(block -> call().bind("id", id)
                                                 .bind("world", block.getWorld().getUID(), StandardValueConverter.UUID_BYTES)
                                                 .bind("x", block.getX())
                                                 .bind("y", block.getY())
                                                 .bind("z", block.getZ())
                                                 .bind("material", block.getType())
                                                 .bind("block_data", block.getBlockData().getAsString())))
                .insert();
        if (fragments != null) {
            fragments = null;
        }
    }

    @MappingProvider({"id", "world"})
    public static RowMapping<Node> map() {
        return row -> new Node(
                row.getLong("id"),
                row.get("world", StandardValueConverter.UUID_BYTES));
    }

    public long id() {
        return id;
    }

    public UUID world() {
        return world;
    }

    public List<Fragment> fragments() {
        if (fragments == null) {
            fragments = query("SELECT * FROM fragments WHERE node_id = :id")
                    .single(call().bind("id", id))
                    .mapAs(Fragment.class)
                    .all();
        }
        return fragments;
    }

    public void breakBlock(@NotNull Block block) {
        query("UPDATE fragments SET destroyed = now() WHERE node_id = :id AND x = :x AND y = :y AND z = :z")
                .single(call().bind("id", id).bind("x", block.getX()).bind("y", block.getY()).bind("z", block.getZ()))
                .update();
    }
}
