package com.flyingpig.jdbc.connection;

import java.sql.Connection;

public class ConnectionHolder {
    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    public static void setConnection(Connection connection) {
        connectionHolder.set(connection);
    }

    public static Connection getConnection() {
        return connectionHolder.get();
    }

    public static void removeConnection() {
        connectionHolder.remove();
    }
}