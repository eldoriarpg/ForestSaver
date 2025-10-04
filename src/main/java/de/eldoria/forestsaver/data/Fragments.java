package de.eldoria.forestsaver.data;

import de.chojo.sadu.queries.converter.StandardValueConverter;
import de.eldoria.forestsaver.configuration.elements.Preset;
import de.eldoria.forestsaver.configuration.elements.ResourceType;
import de.eldoria.forestsaver.data.dao.Fragment;
import de.eldoria.forestsaver.data.dao.GrowingFragment;
import de.eldoria.forestsaver.data.dao.StaticFragment;
import org.bukkit.block.BlockState;

import java.util.List;
import java.util.UUID;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Fragments {
    public List<Fragment> idleFragments(UUID world, ResourceType type) {
        return query("""
                SELECT
                    node_id,
                    resource_type,
                    world,
                    x,
                    y,
                    z,
                    block_data,
                    token,
                    destroyed,
                    always_restore
                FROM
                    fragments
                WHERE destroyed + (token || ' minutes')::INTERVAL < now()
                  AND resource_type = :resource_type
                  AND world = :world::UUID
                  AND node_id IS NULL;
                """)
                .single(call().bind("resource_type", type)
                              .bind("world", world, StandardValueConverter.UUID_STRING))
                .map(row -> switch (row.getEnum("resource_type", ResourceType.class)) {
                    case GROWING -> GrowingFragment.map().map(row);
                    case FRAGMENT -> StaticFragment.map().map(row);
                    default ->
                            throw new RuntimeException("Unknown resource type: " + row.getEnum("resource_type", ResourceType.class));
                })
                .all();
    }

    /**
     * Marks the given block as destroyed.
     *
     * @param block block to mark as destroyed
     */
    public void breakBlock(UUID worldUid, ResourceType resourceType, Preset preset, BlockState block) {
        int token = preset.restoreToken(block);
        query("""
                INSERT
                INTO
                    fragments(resource_type, world, x, y, z, block_data, token, destroyed, always_restore)
                VALUES
                    (:resource_type, :world::UUID, :x, :y, :z, :block_data, :token, now(), FALSE);
                """)
                .single(call()
                        .bind("resource_type", resourceType)
                        .bind("world", worldUid, StandardValueConverter.UUID_STRING)
                        .bind("x", block.getX())
                        .bind("y", block.getY())
                        .bind("z", block.getZ())
                        .bind("block_data", block.getBlockData().getAsString())
                        .bind("token", token))
                .update();
    }
}
