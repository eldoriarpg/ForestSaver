CREATE TABLE forest_schema.nodes (
    id            SERIAL                  NOT NULL
        CONSTRAINT nodes_pk
            PRIMARY KEY,
    world         UUID                    NOT NULL,
    last_modified TIMESTAMP DEFAULT now() NOT NULL
);

CREATE TABLE forest_schema.fragments (
    node_id    BIGINT  NOT NULL,
    world      UUID    NOT NULL,
    x          INTEGER NOT NULL,
    y          INTEGER NOT NULL,
    z          INTEGER NOT NULL,
    material   TEXT    NOT NULL,
    block_data TEXT    NOT NULL,
    destroyed  TIMESTAMP,
    CONSTRAINT fragments_pk
        PRIMARY KEY (world, y, z, x)
);

CREATE FUNCTION forest_schema.update_nodes_last_modified(
) RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE nodes SET last_modified = now() WHERE id = new.node_id;
END;
$$;

CREATE TRIGGER update_last_modified
    AFTER UPDATE OR INSERT
    ON forest_schema.fragments
    WHEN ( new.destroyed IS NOT NULL AND old.destroyed IS NULL )
EXECUTE PROCEDURE forest_schema.update_nodes_last_modified();

