package de.eldoria.forestsaver.data;

import de.chojo.sadu.queries.converter.StandardValueConverter;
import de.eldoria.forestsaver.configuration.Configuration;
import de.eldoria.forestsaver.data.dao.Node;
import dev.chojo.ocular.Configurations;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

public class Nodes {
    //public Map<Long, Node> nodes;
    private final Configurations<Configuration> configurations;

    public Nodes(Configurations<Configuration> configurations) {
        this.configurations = configurations;
    }

    /**
     * Returns the node at the given location.
     *
     * @param location location to get the node for
     * @return node at the given location, or empty if no node exists at the given location
     */
    public Optional<Node> getNode(Location location) {
        return query("""
                SELECT
                    f.world,
                    id,
                    last_modified
                FROM
                    fragments f
                        LEFT JOIN nodes n
                        ON f.node_id = n.id
                WHERE n.world = :uid::UUID
                  AND x = :x
                  AND y = :y
                  AND z = :z
                """)
                .single(call().bind("uid", location.getWorld().getUID(), StandardValueConverter.UUID_STRING)
                              .bind("x", location.getBlockX())
                              .bind("y", location.getBlockY())
                              .bind("z", location.getBlockZ()))
                .mapAs(Node.class)
                .first();
    }

    /**
     * Returns the node with the given id.
     *
     * @param id id of the node to get
     * @return node with the given id, or empty if no node exists with the given id
     */
    public Optional<Node> getNode(long id) {
        return query("SELECT id, last_modified, world FROM nodes WHERE id = :id")
                .single(call().bind("id", id))
                .mapAs(Node.class)
                .first();
    }

    /**
     * Creates a new node in the given world.
     *
     * @param worldUid uid of the world to create the node in
     * @return new node
     */
    public Node createNode(UUID worldUid) {
        return query("INSERT INTO nodes (world) VALUES (:uid::UUID) RETURNING id")
                .single(call().bind("uid", worldUid, StandardValueConverter.UUID_STRING))
                .map(row -> new Node(row.getLong("id"), worldUid))
                .first()
                .orElseThrow(() -> new RuntimeException("Could not create node"));
    }

    /**
     * Returns all nodes of the given world.
     *
     * @param world world to get the nodes for
     * @return list of nodes
     */
    public List<Node> all(World world) {
        return query("SELECT id, last_modified, world FROM nodes WHERE world = :id::UUID")
                .single(call().bind("id", world.getUID(), StandardValueConverter.UUID_STRING))
                .mapAs(Node.class)
                .all();
    }

    /**
     * Returns all idle nodes of the given world.
     *
     * @param world world to get the nodes for
     * @return list of idle nodes
     */
    public List<Node> idleNodes(World world) {
        return query("""
                WITH
                    destroyed AS (
                        SELECT DISTINCT node_id
                        FROM fragments
                        WHERE destroyed IS NOT NULL AND world = :uid::UUID
                        AND destroyed < now() - (:seconds || ' seconds')::INTERVAL
                    )
                SELECT
                    id,
                    last_modified,
                    world
                FROM
                    nodes
                WHERE id IN (
                    SELECT node_id
                    FROM destroyed
                            )
                """)
                .single(call().bind("uid", world.getUID(), StandardValueConverter.UUID_STRING)
                              .bind("seconds", configurations.main().restore().nodeIdleTime()))
                .mapAs(Node.class)
                .all();
    }

    /**
     * Deletes the given node.
     *
     * @param node node to delete
     */
    public void deleteNode(Node node) {
        query("DELETE FROM nodes WHERE id = :id")
                .single(call().bind("id", node.id()))
                .update();
    }

    /**
     * Deletes all unused nodes.
     */
    public void deleteUnusedNodes() {
        query("""
                WITH
                    inactive_nodes AS (
                        SELECT DISTINCT ON (node_id)
                            node_id,
                            bool_and(destroyed IS NULL) AS clear
                        FROM
                            fragments f
                        WHERE f.world = :uid::UUID
                        GROUP BY node_id
                                      ),
                    filtered_nodes AS (
                        SELECT
                            node_id
                        FROM
                            inactive_nodes i
                                LEFT JOIN nodes n
                                ON i.node_id = n.id
                        WHERE clear
                          AND n.last_modified < now() - ( :seconds || ' seconds' )::INTERVAL
                                      )
                DELETE FROM nodes WHERE id IN (filtered_nodes)
                """)
                .single(call().bind("seconds", configurations.main().restore().nodeDeletionTime()))
                .delete();
    }
}
