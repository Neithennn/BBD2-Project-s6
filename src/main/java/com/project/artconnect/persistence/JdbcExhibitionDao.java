package com.project.artconnect.persistence;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Implémentation JDBC du DAO pour les expositions
public class JdbcExhibitionDao implements ExhibitionDao {

    // Construit un objet Exhibition à partir d'une ligne du ResultSet
    private Exhibition lireExhibition(ResultSet rs) throws SQLException {
        Exhibition exhibition = new Exhibition();
        // On remplit l'id pour pouvoir faire des update/delete fiables
        exhibition.setId(rs.getInt("id"));
        exhibition.setTitle(rs.getString("title"));
        exhibition.setDescription(rs.getString("description"));
        exhibition.setCuratorName(rs.getString("curator_name"));
        exhibition.setTheme(rs.getString("theme"));

        // Conversion des dates SQL en LocalDate
        Date dateDebut = rs.getDate("start_date");
        if (dateDebut != null) {
            exhibition.setStartDate(dateDebut.toLocalDate());
        }

        Date dateFin = rs.getDate("end_date");
        if (dateFin != null) {
            exhibition.setEndDate(dateFin.toLocalDate());
        }

        // Création d'une galerie minimale avec son nom
        String nomGalerie = rs.getString("gallery_name");
        if (nomGalerie != null) {
            Gallery gallery = new Gallery();
            gallery.setId(rs.getInt("gallery_id"));
            gallery.setName(nomGalerie);
            exhibition.setGallery(gallery);
        }

        return exhibition;
    }

    // Retourne l'id d'une galerie à partir de son nom
    private int trouverIdGalerie(Connection conn, String nomGalerie) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.prepareStatement("SELECT id FROM galleries WHERE name = ?");
            stmt.setString(1, nomGalerie);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Galerie introuvable : " + nomGalerie);
            }

        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignoré */ }
            }
        }
    }

    @Override
    public List<Exhibition> findAll() {
        List<Exhibition> expositions = new ArrayList<Exhibition>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            // Jointure pour récupérer le nom de la galerie
            String sql = "SELECT e.id, e.title, e.start_date, e.end_date, e.description, "
                       + "e.curator_name, e.theme, g.id AS gallery_id, g.name AS gallery_name "
                       + "FROM exhibitions e "
                       + "JOIN galleries g ON e.gallery_id = g.id";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            // Parcours des résultats
            while (rs.next()) {
                expositions.add(lireExhibition(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur findAll expositions : " + e.getMessage());
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

        return expositions;
    }

    @Override
    public void save(Exhibition exhibition) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rsGenere = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            // Debut de la transaction : resolution de la galerie + insertion = atomique
            conn.setAutoCommit(false);

            // Récupération de l'id de la galerie
            int galerieId = trouverIdGalerie(conn, exhibition.getGallery().getName());

            String sql = "INSERT INTO exhibitions (title, start_date, end_date, description, "
                       + "gallery_id, curator_name, theme) VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, exhibition.getTitle());

            // Conversion de LocalDate en Date SQL
            if (exhibition.getStartDate() != null) {
                stmt.setDate(2, Date.valueOf(exhibition.getStartDate()));
            } else {
                stmt.setNull(2, java.sql.Types.DATE);
            }

            if (exhibition.getEndDate() != null) {
                stmt.setDate(3, Date.valueOf(exhibition.getEndDate()));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }

            stmt.setString(4, exhibition.getDescription());
            stmt.setInt(5, galerieId);
            stmt.setString(6, exhibition.getCuratorName());
            stmt.setString(7, exhibition.getTheme());

            // Exécution de l'insertion
            stmt.executeUpdate();

            // Recuperation de l'id genere
            rsGenere = stmt.getGeneratedKeys();
            if (rsGenere.next()) {
                exhibition.setId(rsGenere.getInt(1));
            }

            // Validation de la transaction
            conn.commit();

        } catch (SQLException e) {
            // En cas d'erreur, on annule tout (aussi en cas de trigger trg_verif_dates_exposition)
            System.err.println("Erreur save exposition : " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignoré */ }
            }
            throw new IllegalStateException("Impossible d'enregistrer l'exposition : " + e.getMessage(), e);
        } finally {
            if (rsGenere != null) {
                try { rsGenere.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { /* ignoré */ }
                try { conn.close(); } catch (SQLException e) { /* ignoré */ }
            }
        }
    }

    @Override
    public void update(Exhibition exhibition) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            // On met a jour par id pour pouvoir aussi renommer l'exposition
            int galerieId = trouverIdGalerie(conn, exhibition.getGallery().getName());

            String sql = "UPDATE exhibitions SET title=?, start_date=?, end_date=?, description=?, "
                       + "gallery_id=?, curator_name=?, theme=? WHERE id=?";
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, exhibition.getTitle());

            if (exhibition.getStartDate() != null) {
                stmt.setDate(2, Date.valueOf(exhibition.getStartDate()));
            } else {
                stmt.setNull(2, java.sql.Types.DATE);
            }

            if (exhibition.getEndDate() != null) {
                stmt.setDate(3, Date.valueOf(exhibition.getEndDate()));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }

            stmt.setString(4, exhibition.getDescription());
            stmt.setInt(5, galerieId);
            stmt.setString(6, exhibition.getCuratorName());
            stmt.setString(7, exhibition.getTheme());

            // Verification : l'id doit etre present
            if (exhibition.getId() == null) {
                throw new SQLException("Impossible de mettre a jour une exposition sans id.");
            }
            stmt.setInt(8, exhibition.getId());

            // Exécution de la mise à jour
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur update exposition : " + e.getMessage());
            throw new IllegalStateException("Impossible de mettre a jour l'exposition : " + e.getMessage(), e);
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { /* ignoré */ }
            }
        }
    }

    @Override
    public void delete(String title) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("DELETE FROM exhibitions WHERE title = ?");
            stmt.setString(1, title);

            // Exécution de la suppression
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur delete exposition : " + e.getMessage());
            throw new IllegalStateException("Impossible de supprimer l'exposition : " + e.getMessage(), e);
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { /* ignoré */ }
            }
        }
    }
}
