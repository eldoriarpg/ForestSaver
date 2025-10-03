package de.eldoria.forestsaver.configuration.elements;

public class Database {
    DatabaseType type = DatabaseType.SQLITE;
    String host = "localhost";
    int port = 3306;
    String database = "forestsaver";
    String username = "root";
    String password = "";
    // for sqlite
    String path = "forestsaver.db";
    // for postgres
    String schema = "forestsaver";
    // For mariadb and mysql
    String tablePrefix = "fs_";
    int poolSize = 3;

    public DatabaseType type() {
        return type;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    /**
     * Name of the database.
     * Applicable when using {@link DatabaseType#POSTGRESQL}, {@link DatabaseType#MYSQL} or {@link DatabaseType#MARIADB}.
     *
     * @return database
     */
    public String database() {
        return database;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    /**
     * Path to the database file.
     * Applicable when using {@link DatabaseType#SQLITE}.
     *
     * @return path
     */
    public String path() {
        return path;
    }

    /**
     * Schema for all tables in the database.
     * Applicable when using {@link DatabaseType#POSTGRESQL}.
     *
     * @return schema
     */

    public String schema() {
        return schema;
    }

    /**
     * Table prefix for all tables in the database.
     * Applicable when using {@link DatabaseType#MYSQL} or {@link DatabaseType#MARIADB}.
     *
     * @return tablePrefix
     */
    public String tablePrefix() {
        return tablePrefix;
    }

    /**
     * The size of the connection pool.
     * Applicable when using {@link DatabaseType#POSTGRESQL} {@link DatabaseType#MYSQL}, {@link DatabaseType#MARIADB}.
     *
     * @return poolSize
     */
    public int poolSize() {
        return poolSize;
    }
}
