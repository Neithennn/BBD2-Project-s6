package com.project.artconnect.util;

import com.project.artconnect.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Classe utilitaire pour obtenir une connexion JDBC
public class ConnectionManager {

    // Retourne une nouvelle connexion à la base de données
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            DatabaseConfig.URL,
            DatabaseConfig.USER,
            DatabaseConfig.PASSWORD
        );
    }
}
