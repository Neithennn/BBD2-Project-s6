package com.project.artconnect.persistence;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Implémentation JDBC du DAO pour les ateliers
public class JdbcWorkshopDao implements WorkshopDao {

    // Construit un objet Workshop à partir d'une ligne du ResultSet
    private Workshop lireWorkshop(ResultSet rs) throws SQLException {
        Workshop workshop = new Workshop();
        workshop.setTitle(rs.getString("title"));
        workshop.setDurationMinutes(rs.getInt("duration_minutes"));
        workshop.setMaxParticipants(rs.getInt("max_participants"));
        workshop.setPrice(rs.getDouble("price"));
        workshop.setLocation(rs.getString("location"));
        workshop.setDescription(rs.getString("description"));
        workshop.setLevel(rs.getString("level"));

        // Conversion du Timestamp SQL en LocalDateTime
        Timestamp date = rs.getTimestamp("date");
        if (date != null) {
            workshop.setDate(date.toLocalDateTime());
        }

        // Création d'un artiste minimal avec son nom (l'instructeur)
        String nomInstructeur = rs.getString("instructor_name");
        if (nomInstructeur != null) {
            Artist instructeur = new Artist();
            instructeur.setName(nomInstructeur);
            workshop.setInstructor(instructeur);
        }

        return workshop;
    }

    @Override
    public Optional<Workshop> findById(Long id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            // Jointure pour récupérer le nom de l'instructeur
            String sql = "SELECT w.title, w.date, w.duration_minutes, w.max_participants, "
                       + "w.price, w.location, w.description, w.level, "
                       + "ar.name AS instructor_name "
                       + "FROM workshops w "
                       + "JOIN artists ar ON w.instructor_id = ar.id "
                       + "WHERE w.id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            rs = stmt.executeQuery();

            // Retourne l'atelier s'il existe
            if (rs.next()) {
                return Optional.of(lireWorkshop(rs));
            } else {
                return Optional.empty();
            }

        } catch (SQLException e) {
            System.err.println("Erreur findById atelier : " + e.getMessage());
            return Optional.empty();
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { /* ignoré */ }
            }
        }
    }

    @Override
    public List<Workshop> findAll() {
        List<Workshop> ateliers = new ArrayList<Workshop>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            // Jointure pour récupérer le nom de l'instructeur
            String sql = "SELECT w.title, w.date, w.duration_minutes, w.max_participants, "
                       + "w.price, w.location, w.description, w.level, "
                       + "ar.name AS instructor_name "
                       + "FROM workshops w "
                       + "JOIN artists ar ON w.instructor_id = ar.id";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            // Parcours des résultats
            while (rs.next()) {
                ateliers.add(lireWorkshop(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur findAll ateliers : " + e.getMessage());
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { /* ignoré */ }
            }
        }

        return ateliers;
    }
}
