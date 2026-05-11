package com.project.artconnect.config;

public class DatabaseConfig {
    private static final String DEFAULT_URL =
        "jdbc:mysql://localhost:3306/ArtConnect?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Europe/Paris";

    public static final String URL = readConfig("artconnect.db.url", "ARTCONNECT_DB_URL", DEFAULT_URL);
    public static final String USER = readConfig("artconnect.db.user", "ARTCONNECT_DB_USER", "root");
    public static final String PASSWORD = readConfig("artconnect.db.password", "ARTCONNECT_DB_PASSWORD", "root");

    private DatabaseConfig() {
    }

    private static String readConfig(String systemProperty, String environmentVariable, String defaultValue) {
        String value = System.getProperty(systemProperty);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }

        value = System.getenv(environmentVariable);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }

        return defaultValue;
    }
}
