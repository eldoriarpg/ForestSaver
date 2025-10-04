package de.eldoria.forestsaver.data.dao;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.converter.StandardValueConverter;
import de.eldoria.forestsaver.configuration.elements.ResourceType;
import org.bukkit.World;
import org.bukkit.util.BlockVector;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static de.chojo.sadu.queries.api.query.Query.query;
import static de.chojo.sadu.queries.converter.StandardValueConverter.UUID_STRING;

public class StaticFragment extends Fragment {
    public StaticFragment(ResourceType type, UUID world, BlockVector position, String data, boolean alwaysRestorable, Instant destroyed, int token) {
        super(type, world, position, data, alwaysRestorable, destroyed, token);
    }

    @MappingProvider({"resource_type", "world", "x", "y", "z", "block_data", "always_restore", "destroyed", "token"})
    public static RowMapping<StaticFragment> map() {
        return row -> new StaticFragment(ResourceType.FRAGMENT,
                row.get("world", UUID_STRING),
                new BlockVector(row.getInt("x"),
                        row.getInt("y"),
                        row.getInt("z")),
                row.getString("block_data"),
                row.getBoolean("always_restore"),
                row.get("destroyed", StandardValueConverter.INSTANT_TIMESTAMP),
                row.getInt("token"));
    }

    @Override
    public void restore(World world) {
        // TODO: logging
        // TODO: Maybe sync back to main thread?
        CompletableFuture.runAsync(() -> query("DELETE FROM fragments WHERE world = :world AND x = :x AND y = :y AND z = :z;"));
        world.setBlockData(position.toLocation(world), blockData());
    }

}
