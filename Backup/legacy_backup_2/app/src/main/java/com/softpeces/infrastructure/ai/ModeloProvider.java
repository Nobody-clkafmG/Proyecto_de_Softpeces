package com.softpeces.infrastructure.ai;

public class ModeloProvider {
    private final String path;
    private final String version;
    public ModeloProvider(String path, String version) { this.path = path; this.version = version; }
    public Object cargar() { return new Object(); }
}
