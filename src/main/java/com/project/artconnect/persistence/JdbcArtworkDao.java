package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// Implémentation JDBC du DAO pour les oeuvres
public class JdbcArtworkDao implements ArtworkDao {

    // Construit un objet Artwork à partir d'une ligne du ResultSet
    private Artwork lireArtwork(ResultSet rs) throws SQLException {
        Artwork artwork = new Artwork();
        // On remplit l'id pour pouvoir faire des update/delete fiables
        artwork.setId(rs.getInt("id"));
        artwork.setTitle(rs.getString("title"));
        artwork.setCreationYear(rs.getObject("creation_year") != null ? rs.getInt("creation_year") : null);
        artwork.setType(rs.getString("type"));
        artwork.setMedium(rs.getString("medium"));
        artwork.setDimensions(rs.getString("dimensions"));
        artwork.setDescription(rs.getString("description"));
        artwork.setPrice(rs.getDouble("price"));

        // Conversion du statut texte en enum
        String statutTexte = rs.getString("status");
        if (statutTexte != null) {
            artwork.setStatus(Artwork.Status.valueOf(statutTexte));
        }

        // Création d'un artiste minimal avec son nom
        String nomArtiste = rs.getString("artist_name");
        if (nomArtiste != null) {
            Artist artist = new Artist();
            artist.setId(rs.getInt("artist_id"));
            artist.setName(nomArtiste);
            artwork.setArtist(artist);
        }

        return artwork;
    }

    // Retourne l'id d'un artiste à partir de son nom
    private int trouverIdArtiste(Connection conn, String nomArtiste) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.prepareStatement("SELECT id FROM artists WHERE name = ?");
            stmt.setString(1, nomArtiste);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Artiste introuvable : " + nomArtiste);
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
    public List<Artwork> findAll() {
        List<Artwork> oeuvres = new ArrayList<Artwork>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            // Jointure pour récupérer le nom de l'artiste
            String sql = "SELECT a.id, a.title, a.creation_year, a.type, a.medium, a.dimensions, "
                       + "a.description, a.price, a.status, ar.id AS artist_id, ar.name AS artist_name "
                       + "FROM artworks a "
                       + "JOIN artists ar ON a.artist_id = ar.id";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            // Parcours des résultats
            while (rs.next()) {
                oeuvres.add(lireArtwork(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur findAll oeuvres : " + e.getMessage());
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

        return oeuvres;
    }

    @Override
    public void save(Artwork artwork) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rsGenere = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            // Debut de la transaction : on veut que la resolution de l'artiste
            // et l'insertion de l'oeuvre soient atomiques
            conn.setAutoCommit(false);

            // Récupération de l'id de l'artiste
            int artistId = trouverIdArtiste(conn, artwork.getArtist().getName());

            String sql = "INSERT INTO artworks (title, creation_year, type, medium, dimensions, "
                       + "description, price, status, artist_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, artwork.getTitle());

            if (artwork.getCreationYear() == null) {
                stmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(2, artwork.getCreationYear());
            }

            stmt.setString(3, artwork.getType());
            stmt.setString(4, artwork.getMedium());
            stmt.setString(5, artwork.getDimensions());
            stmt.setString(6, artwork.getDescription());
            stmt.setDouble(7, artwork.getPrice());

            // Conversion de l'enum en texte
            if (artwork.getStatus() == null) {
                stmt.setString(8, "FOR_SALE");
            } else {
                stmt.setString(8, artwork.getStatus().name());
            }

            stmt.setInt(9, artistId);

            // Exécution de l'insertion
            stmt.executeUpdate();

            // Recuperation de l'id genere
            rsGenere = stmt.getGeneratedKeys();
            if (rsGenere.next()) {
                artwork.setId(rsGenere.getInt(1));
            }

            // Validation de la transaction
            conn.commit();

        } catch (SQLException e) {
            // En cas d'erreur, on annule toute la transaction
            System.err.println("Erreur save oeuvre : " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignoré */ }
            }
            throw new IllegalStateException("Impossible d'enregistrer l'oeuvre : " + e.getMessage(), e);
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
    public void update(Artwork artwork) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            // On met a jour par id pour pouvoir aussi renommer l'oeuvre
            int artistId = trouverIdArtiste(conn, artwork.getArtist().getName());

            String sql = "UPDATE artworks SET title=?, creation_year=?, type=?, medium=?, dimensions=?, "
                       + "description=?, price=?, status=?, artist_id=? WHERE id=?";
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, artwork.getTitle());

            if (artwork.getCreationYear() == null) {
                stmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(2, artwork.getCreationYear());
            }

            stmt.setString(3, artwork.getType());
            stmt.setString(4, artwork.getMedium());
            stmt.setString(5, artwork.getDimensions());
            stmt.setString(6, artwork.getDescription());
            stmt.setDouble(7, artwork.getPrice());

            if (artwork.getStatus() == null) {
                stmt.setString(8, "FOR_SALE");
            } else {
                stmt.setString(8, artwork.getStatus().name());
            }

            stmt.setInt(9, artistId);

            // Verification : l'id doit etre present (l'oeuvre doit venir de la BD)
            if (artwork.getId() == null) {
                throw new SQLException("Impossible de mettre a jour une oeuvre sans id.");
            }
            stmt.setInt(10, artwork.getId());

            // Exécution de la mise à jour
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur update oeuvre : " + e.getMessage());
            throw new IllegalStateException("Impossible de mettre a jour l'oeuvre : " + e.getMessage(), e);
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
            stmt = conn.prepareStatement("DELETE FROM artworks WHERE title = ?");
            stmt.setString(1, title);

            // Exécution de la suppression
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur delete oeuvre : " + e.getMessage());
            throw new IllegalStateException("Impossible de supprimer l'oeuvre : " + e.getMessage(), e);
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
    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> oeuvres = new ArrayList<Artwork>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            String sql = "SELECT a.id, a.title, a.creation_year, a.type, a.medium, a.dimensions, "
                       + "a.description, a.price, a.status, ar.id AS artist_id, ar.name AS artist_name "
                       + "FROM artworks a "
                       + "JOIN artists ar ON a.artist_id = ar.id "
                       + "WHERE ar.name = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, artistName);
            rs = stmt.executeQuery();

            // Parcours des résultats
            while (rs.next()) {
                oeuvres.add(lireArtwork(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur findByArtistName oeuvres : " + e.getMessage());
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

        return oeuvres;
    }
}
