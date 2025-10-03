CREATE TABLE forest_schema.nodes (
    id            SERIAL                  NOT NULL
        CONSTRAINT nodes_pk
            PRIMARY KEY,
    world         UUID                    NOT NULL,
    last_modified TIMESTAMP DEFAULT now() NOT NULL
);

CREATE TABLE forest_schema.fragments (
    node_id        BIGINT  NOT NULL,
    world          UUID    NOT NULL,
    x              INTEGER NOT NULL,
    y              INTEGER NOT NULL,
    z              INTEGER NOT NULL,
    block_data     TEXT    NOT NULL,
    destroyed      TIMESTAMP,
    always_restore BOOLEAN
        CONSTRAINT fragments_pk
            PRIMARY KEY (world, y, z, x)
);

DROP INDEX IF EXISTS forest_schema.fragments_world_node_id_idx;
CREATE INDEX fragments_world_node_id_idx
    ON forest_schema.fragments (world) INCLUDE (node_id, destroyed) WHERE destroyed IS NOT NULL;

CREATE OR REPLACE FUNCTION forest_schema.update_nodes_last_modified(
) RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE forest_schema.nodes SET last_modified = now() WHERE id = old.node_id;
    RETURN new;
END;
$$;

CREATE OR REPLACE TRIGGER update_last_modified
    AFTER UPDATE OF destroyed OR INSERT
    ON forest_schema.fragments
EXECUTE PROCEDURE forest_schema.update_nodes_last_modified();

ALTER TABLE forest_schema.fragments
    ADD CONSTRAINT fragments_nodes_id_fk
        FOREIGN KEY (node_id) REFERENCES forest_schema.nodes
            ON DELETE CASCADE;

