package de.eldoria.forestsaver.data;

import de.chojo.sadu.queries.converter.StandardValueConverter;
import de.eldoria.forestsaver.data.dao.Node;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Nodes {
    //public Map<Long, Node> nodes;

    public Nodes() {
    }

    public Optional<Node> getNode(Location location) {
        return query("SELECT * FROM fragments f LEFT JOIN nodes n ON f.node_id = n.id WHERE n.world = :uid::UUID AND x = :x AND y = :y AND z = :z")
                .single(call().bind("uid", location.getWorld().getUID(), StandardValueConverter.UUID_STRING)
                              .bind("x", location.getBlockX())
                              .bind("y", location.getBlockY())
                              .bind("z", location.getBlockZ()))
                .mapAs(Node.class)
                .first();
    }

    public Optional<Node> getNode(long id) {
        return query("SELECT * FROM nodes WHERE id = :id")
                .single(call().bind("id", id))
                .mapAs(Node.class)
                .first();
    }

    public Node createNode(UUID worldUid) {
        return query("INSERT INTO nodes (world) VALUES (:uid::UUID) RETURNING id")
                .single(call().bind("uid", worldUid, StandardValueConverter.UUID_STRING))
                .map(row -> new Node(row.getLong("id"), worldUid))
                .first()
                .orElseThrow(() -> new RuntimeException("Could not create node"));
    }

    public List<Node> all(World world) {
        return query("SELECT * FROM nodes WHERE world = :id::UUID")
                .single(call().bind("id", world.getUID(), StandardValueConverter.UUID_STRING))
                .mapAs(Node.class)
                .all();
    }
}
