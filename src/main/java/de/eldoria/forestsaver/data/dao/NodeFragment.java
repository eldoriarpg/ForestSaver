package de.eldoria.forestsaver.data.dao;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.converter.StandardValueConverter;
import de.eldoria.forestsaver.configuration.elements.ResourceType;
import org.bukkit.util.BlockVector;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static de.chojo.sadu.queries.converter.StandardValueConverter.UUID_STRING;

public class NodeFragment extends Fragment {
    protected final long nodeId;

    public NodeFragment(long nodeId, ResourceType type, UUID world, BlockVector position, String data,
                        boolean alwaysRestorable, Instant destroyed, int token) {
        super(type, world, position, data, alwaysRestorable, destroyed, token);
        this.nodeId = nodeId;
    }

    @MappingProvider({"node_id", "resource_type", "world", "x", "y", "z", "block_data", "always_restore", "destroyed", "token"})
    public static RowMapping<NodeFragment> map() {
        return row -> new NodeFragment(row.getLong("node_id"),
                row.getEnum("resource_type", ResourceType.class),
                row.get("world", UUID_STRING),
                new BlockVector(row.getInt("x"),
                        row.getInt("y"),
                        row.getInt("z")),
                row.getString("block_data"),
                row.getBoolean("always_restore"),
                row.get("destroyed", StandardValueConverter.INSTANT_TIMESTAMP),
                row.getInt("token"));
    }

    public long nodeId() {
        return nodeId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (NodeFragment) obj;
        return this.nodeId == that.nodeId &&
               Objects.equals(this.type, that.type) &&
               Objects.equals(this.world, that.world) &&
               Objects.equals(this.position, that.position) &&
               Objects.equals(this.data, that.data) &&
               this.alwaysRestorable == that.alwaysRestorable &&
               Objects.equals(this.destroyed, that.destroyed) &&
               this.token == that.token;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, type, world, position, data, alwaysRestorable, destroyed, token);
    }
}
