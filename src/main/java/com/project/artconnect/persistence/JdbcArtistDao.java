package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Implémentation JDBC du DAO pour les artistes
public class JdbcArtistDao implements ArtistDao {

    // Charge les disciplines d'un artiste depuis la base
    private List<Discipline> chargerDisciplines(Connection conn, int artistId) throws SQLException {
        List<Discipline> disciplines = new ArrayList<Discipline>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT d.id, d.name FROM disciplines d "
                       + "JOIN artist_disciplines ad ON d.id = ad.discipline_id "
                       + "WHERE ad.artist_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, artistId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                disciplines.add(new Discipline(rs.getInt("id"), rs.getString("name")));
            }
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignoré */ }
            }
        }

        return disciplines;
    }

    // Construit un objet Artist à partir d'une ligne du ResultSet
    private Artist lireArtist(ResultSet rs) throws SQLException {
        Artist artist = new Artist();
        // On remplit l'id pour pouvoir faire des update/delete fiables
        artist.setId(rs.getInt("id"));
        artist.setName(rs.getString("name"));
        artist.setBio(rs.getString("bio"));
        artist.setBirthYear(rs.getObject("birth_year") != null ? rs.getInt("birth_year") : null);
        artist.setContactEmail(rs.getString("contact_email"));
        artist.setPhone(rs.getString("phone"));
        artist.setCity(rs.getString("city"));
        artist.setWebsite(rs.getString("website"));
        artist.setSocialMedia(rs.getString("social_media"));
        artist.setActive(rs.getInt("is_active") == 1);
        return artist;
    }

    @Override
    public List<Artist> findAll() {
        List<Artist> artistes = new ArrayList<Artist>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            String sql = "SELECT id, name, bio, birth_year, contact_email, phone, "
                       + "city, website, social_media, is_active FROM artists";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            // Parcours des résultats
            while (rs.next()) {
                Artist artist = lireArtist(rs);
                // Chargement des disciplines de cet artiste
                List<Discipline> disciplines = chargerDisciplines(conn, artist.getId());
                artist.setDisciplines(disciplines);
                artistes.add(artist);
            }

        } catch (SQLException e) {
            System.err.println("Erreur findAll artistes : " + e.getMessage());
        } finally {
            // Fermeture des ressources
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

        return artistes;
    }

    @Override
    public void save(Artist artist) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rsGenere = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            // Debut de la transaction : artiste + disciplines doivent etre atomiques
            conn.setAutoCommit(false);

            String sql = "INSERT INTO artists (name, bio, birth_year, contact_email, phone, "
                       + "city, website, social_media, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Remplissage des paramètres
            stmt.setString(1, artist.getName());
            stmt.setString(2, artist.getBio());

            if (artist.getBirthYear() == null) {
                stmt.setNull(3, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(3, artist.getBirthYear());
            }

            stmt.setString(4, artist.getContactEmail());
            stmt.setString(5, artist.getPhone());
            stmt.setString(6, artist.getCity());
            stmt.setString(7, artist.getWebsite());
            stmt.setString(8, artist.getSocialMedia());
            stmt.setInt(9, artist.isActive() ? 1 : 0);

            // Exécution de l'insertion
            stmt.executeUpdate();

            // Récupération de l'id généré pour les disciplines
            rsGenere = stmt.getGeneratedKeys();
            if (rsGenere.next()) {
                int artistId = rsGenere.getInt(1);
                // On stocke l'id dans l'objet pour les update/delete ulterieurs
                artist.setId(artistId);
                sauvegarderDisciplines(conn, artistId, artist.getDisciplines());
            }

            // Validation de la transaction (les 2 inserts ont reussi)
            conn.commit();

        } catch (SQLException e) {
            // En cas d'erreur, on annule toute la transaction
            System.err.println("Erreur save artiste : " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignoré */ }
            }
            throw new IllegalStateException("Impossible d'enregistrer l'artiste : " + e.getMessage(), e);
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

    // Insère les disciplines d'un artiste dans la table de jonction
    private void sauvegarderDisciplines(Connection conn, int artistId, List<Discipline> disciplines)
            throws SQLException {
        if (disciplines == null || disciplines.isEmpty()) {
            return;
        }

        for (Discipline discipline : disciplines) {
            // Chercher l'id de la discipline par son nom
            int disciplineId = trouverOuCreerDiscipline(conn, discipline.getName());

            // Insérer dans la table de jonction
            PreparedStatement stmtJonction = null;
            try {
                String sql = "INSERT IGNORE INTO artist_disciplines (artist_id, discipline_id) VALUES (?, ?)";
                stmtJonction = conn.prepareStatement(sql);
                stmtJonction.setInt(1, artistId);
                stmtJonction.setInt(2, disciplineId);
                stmtJonction.executeUpdate();
            } finally {
                if (stmtJonction != null) {
                    try { stmtJonction.close(); } catch (SQLException e) { /* ignoré */ }
                }
            }
        }
    }

    // Retourne l'id d'une discipline, la crée si elle n'existe pas
    private int trouverOuCreerDiscipline(Connection conn, String nom) throws SQLException {
        PreparedStatement stmtSelect = null;
        PreparedStatement stmtInsert = null;
        ResultSet rs = null;
        ResultSet rsGenere = null;

        try {
            // Chercher la discipline existante
            stmtSelect = conn.prepareStatement("SELECT id FROM disciplines WHERE name = ?");
            stmtSelect.setString(1, nom);
            rs = stmtSelect.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

            // Créer la discipline si elle n'existe pas
            stmtInsert = conn.prepareStatement(
                "INSERT INTO disciplines (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            stmtInsert.setString(1, nom);
            stmtInsert.executeUpdate();
            rsGenere = stmtInsert.getGeneratedKeys();

            if (rsGenere.next()) {
                return rsGenere.getInt(1);
            }

        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (rsGenere != null) {
                try { rsGenere.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (stmtSelect != null) {
                try { stmtSelect.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (stmtInsert != null) {
                try { stmtInsert.close(); } catch (SQLException e) { /* ignoré */ }
            }
        }

        throw new SQLException("Impossible de trouver ou créer la discipline : " + nom);
    }

    @Override
    public void update(Artist artist) {
        if (artist.getId() == null) {
            throw new IllegalArgumentException("Impossible de mettre a jour un artiste sans id.");
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement deleteDisciplinesStmt = null;
        boolean transactional = false;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            // On met a jour par id pour pouvoir aussi modifier le nom sans casser le WHERE
            conn.setAutoCommit(false);
            transactional = true;

            String sql = "UPDATE artists SET name=?, bio=?, birth_year=?, contact_email=?, phone=?, "
                       + "city=?, website=?, social_media=?, is_active=? WHERE id=?";
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, artist.getName());
            stmt.setString(2, artist.getBio());

            if (artist.getBirthYear() == null) {
                stmt.setNull(3, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(3, artist.getBirthYear());
            }

            stmt.setString(4, artist.getContactEmail());
            stmt.setString(5, artist.getPhone());
            stmt.setString(6, artist.getCity());
            stmt.setString(7, artist.getWebsite());
            stmt.setString(8, artist.getSocialMedia());
            stmt.setInt(9, artist.isActive() ? 1 : 0);

            // Verification : l'id doit etre present (l'artiste doit venir de la BD)
            if (artist.getId() == null) {
                throw new SQLException("Impossible de mettre a jour un artiste sans id.");
            }
            stmt.setInt(10, artist.getId());

            stmt.executeUpdate();

            deleteDisciplinesStmt = conn.prepareStatement("DELETE FROM artist_disciplines WHERE artist_id = ?");
            deleteDisciplinesStmt.setInt(1, artist.getId());
            deleteDisciplinesStmt.executeUpdate();
            sauvegarderDisciplines(conn, artist.getId(), artist.getDisciplines());

            conn.commit();

        } catch (SQLException e) {
            System.err.println("Erreur update artiste : " + e.getMessage());
            if (conn != null && transactional) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            throw new IllegalStateException("Impossible de mettre a jour l'artiste : " + e.getMessage(), e);
        } finally {
            if (deleteDisciplinesStmt != null) {
                try { deleteDisciplinesStmt.close(); } catch (SQLException e) { /* ignore */ }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { /* ignoré */ }
            }
            if (conn != null) {
                if (transactional) {
                    try { conn.setAutoCommit(true); } catch (SQLException e) { /* ignore */ }
                }
                try { conn.close(); } catch (SQLException e) { /* ignoré */ }
            }
        }
    }

    @Override
    public void delete(String artistName) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            // Les cascades suppriment artworks et artist_disciplines automatiquement
            stmt = conn.prepareStatement("DELETE FROM artists WHERE name = ?");
            stmt.setString(1, artistName);

            // Exécution de la suppression
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur delete artiste : " + e.getMessage());
            throw new IllegalStateException("Impossible de supprimer l'artiste : " + e.getMessage(), e);
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
    public List<Artist> findByCity(String city) {
        List<Artist> artistes = new ArrayList<Artist>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            String sql = "SELECT id, name, bio, birth_year, contact_email, phone, "
                       + "city, website, social_media, is_active FROM artists WHERE city = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, city);
            rs = stmt.executeQuery();

            // Parcours des résultats
            while (rs.next()) {
                Artist artist = lireArtist(rs);
                List<Discipline> disciplines = chargerDisciplines(conn, artist.getId());
                artist.setDisciplines(disciplines);
                artistes.add(artist);
            }

        } catch (SQLException e) {
            System.err.println("Erreur findByCity artiste : " + e.getMessage());
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

        return artistes;
    }
}
