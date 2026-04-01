package com.project.artconnect.persistence;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Implémentation JDBC du DAO pour les galeries
public class JdbcGalleryDao implements GalleryDao {

    // Construit un objet Gallery à partir d'une ligne du ResultSet
    private Gallery lireGallery(ResultSet rs) throws SQLException {
        Gallery gallery = new Gallery();
        gallery.setName(rs.getString("name"));
        gallery.setAddress(rs.getString("address"));
        gallery.setOwnerName(rs.getString("owner_name"));
        gallery.setOpeningHours(rs.getString("opening_hours"));
        gallery.setContactPhone(rs.getString("contact_phone"));
        gallery.setRating(rs.getDouble("rating"));
        gallery.setWebsite(rs.getString("website"));
        return gallery;
    }

    @Override
    public Optional<Gallery> findById(Long id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            String sql = "SELECT id, name, address, owner_name, opening_hours, "
                       + "contact_phone, rating, website FROM galleries WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            rs = stmt.executeQuery();

            // Retourne la galerie si elle existe
            if (rs.next()) {
                return Optional.of(lireGallery(rs));
            } else {
                return Optional.empty();
            }

        } catch (SQLException e) {
            System.err.println("Erreur findById galerie : " + e.getMessage());
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
    public List<Gallery> findAll() {
        List<Gallery> galeries = new ArrayList<Gallery>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            String sql = "SELECT id, name, address, owner_name, opening_hours, "
                       + "contact_phone, rating, website FROM galleries";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            // Parcours des résultats
            while (rs.next()) {
                galeries.add(lireGallery(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur findAll galeries : " + e.getMessage());
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

        return galeries;
    }
}
