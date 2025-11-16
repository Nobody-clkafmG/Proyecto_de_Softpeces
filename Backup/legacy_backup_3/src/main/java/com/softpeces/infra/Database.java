package com.softpeces.infra;

import java.nio.file.*;
import java.sql.*;

public final class Database {
    private static final String DB_DIR = "data";
    private static final String DB_PATH = DB_DIR + "/softpeces.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_PATH;

    public static String getJdbcUrl() {
        return JDBC_URL;
    }

    public static Connection get() throws SQLException {
        try { Files.createDirectories(Path.of(DB_DIR)); } catch (Exception ignored) {}
        Connection conn = DriverManager.getConnection(JDBC_URL);
        try (Statement st = conn.createStatement()) { st.execute("PRAGMA foreign_keys = ON;"); }
        return conn;
    }
}
