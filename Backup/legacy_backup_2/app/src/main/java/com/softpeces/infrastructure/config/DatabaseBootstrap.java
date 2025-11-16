package com.softpeces.infrastructure.config;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Scanner;

public class DatabaseBootstrap {
    private final DataSource ds;
    private final String schemaPath;

    public DatabaseBootstrap(DataSource ds, String schemaPath) {
        this.ds = ds; this.schemaPath = schemaPath;
    }

    public void init() {
        try (InputStream in = getClass().getResourceAsStream(schemaPath)) {
            if (in == null) return;
            String sql = new Scanner(in, "UTF-8").useDelimiter("\\A").next();
            try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
                st.execute(sql);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error ejecutando schema.sql", e);
        }
    }
}
