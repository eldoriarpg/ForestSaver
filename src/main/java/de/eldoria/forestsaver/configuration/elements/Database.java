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

    public String database() {
        return database;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public String path() {
        return path;
    }

    public String schema() {
        return schema;
    }

    public String tablePrefix() {
        return tablePrefix;
    }

    public int poolSize() {
        return poolSize;
    }
}
