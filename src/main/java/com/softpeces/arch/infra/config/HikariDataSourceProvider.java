package com.softpeces.arch.infra.config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Placeholder DataSource provider aligned with the diagram.
 * Internally delegates to DriverManager for the existing SQLite URL.
 * If you later switch to HikariCP, replace this class' get() method.
 */
public final class HikariDataSourceProvider {
    private static final String JDBC_URL = "jdbc:sqlite:data/softpeces.db";

    private HikariDataSourceProvider() {}

    public static DataSource get() {
        return new DataSource() {
            @Override public Connection getConnection() throws SQLException { return DriverManager.getConnection(JDBC_URL); }
            @Override public Connection getConnection(String u, String p) throws SQLException { return DriverManager.getConnection(JDBC_URL); }
            @Override public PrintWriter getLogWriter() { return null; }
            @Override public void setLogWriter(PrintWriter out) {}
            @Override public void setLoginTimeout(int seconds) {}
            @Override public int getLoginTimeout() { return 0; }
            @Override public Logger getParentLogger() { return Logger.getLogger("softpeces"); }
            @Override public <T> T unwrap(Class<T> iface) { return null; }
            @Override public boolean isWrapperFor(Class<?> iface) { return false; }
        };
    }
}
