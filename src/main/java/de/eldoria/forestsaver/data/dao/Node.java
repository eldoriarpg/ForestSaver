package de.eldoria.forestsaver.data.dao;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.converter.StandardValueConverter;
import de.eldoria.forestsaver.configuration.elements.Preset;
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

    public void addBlocks(List<BlockState> blocks, Preset preset) {
        query("INSERT INTO fragments (node_id, world, x, y, z, block_data, always_restore) VALUES (:id, :world::UUID, :x, :y, :z, :block_data, :always_restore) ON CONFLICT(world, x, y, z) DO NOTHING")
                .batch(blocks.stream()
                             .map(block -> call().bind("id", id)
                                                 .bind("world", block.getWorld().getUID(), StandardValueConverter.UUID_STRING)
                                                 .bind("x", block.getX())
                                                 .bind("y", block.getY())
                                                 .bind("z", block.getZ())
                                                 .bind("block_data", block.getBlockData().getAsString())
                                                 .bind("always_restore", preset.alwaysRestore(block.getType()))))
                .insert();
        if (fragments != null) {
            fragments = null;
        }
    }

    @MappingProvider({"id", "world"})
    public static RowMapping<Node> map() {
        return row -> new Node(
                row.getLong("id"),
                row.get("world", StandardValueConverter.UUID_STRING));
    }

    public long id() {
        return id;
    }

    public UUID world() {
        return world;
    }

    public List<Fragment> fragments() {
        if (fragments == null) {
            fragments = query("SELECT node_id, world, x, y, z, block_data, always_restore, destroyed FROM fragments WHERE node_id = :id")
                    .single(call().bind("id", id))
                    .mapAs(Fragment.class)
                    .all();
        }
        return fragments;
    }

    public List<Fragment> destroyedFragments() {
        return query("SELECT node_id, world, x, y, z, block_data, always_restore, destroyed FROM fragments WHERE node_id = :id AND (destroyed IS NOT NULL OR always_restore)")
                .single(call().bind("id", id))
                .mapAs(Fragment.class)
                .all();
    }

    public void breakBlock(@NotNull Block block) {
        query("""
                UPDATE fragments SET destroyed = now() WHERE node_id = :id AND x = :x AND y = :y AND z = :z;
                UPDATE nodes SET last_modified = now() WHERE id = :id;""")
                .single(call().bind("id", id).bind("x", block.getX()).bind("y", block.getY()).bind("z", block.getZ()))
                .update();
    }
}
