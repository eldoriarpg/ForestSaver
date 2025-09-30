package de.eldoria.forestsaver.data.data;

import de.chojo.sadu.queries.converter.StandardValueConverter;
import org.bukkit.Location;

import java.util.Optional;
import java.util.UUID;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class World {
    private UUID worldUid;

    public Optional<Node> getNode(Location location) {
        return query("SELECT * FROM fragments f LEFT JOIN nodes n ON f.node_id = n.id WHERE n.world = :uid AND x = :x AND y = :y AND z = :z")
                .single(call().bind("uid", location.getWorld().getUID(), StandardValueConverter.UUID_BYTES)
                              .bind("x", location.getBlockX())
                              .bind("y", location.getBlockY())
                              .bind("z", location.getBlockZ()))
                .mapAs(Node.class)
                .first();
    }

    public Node getOrCreateNode(Location location){
        return getNode(location).orElseGet(this::createNode);
    }

    public Node createNode() {
        return query("INSERT INTO nodes (world) VALUES (:uid) RETURNING id")
                .single(call().bind("uid", worldUid, StandardValueConverter.UUID_BYTES))
                .map(row -> new Node(row.getLong("id"), worldUid))
                .first()
                .orElseThrow(() -> new RuntimeException("Could not create node"));
    }
}
