package com.project.artconnect.service.impl;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.persistence.JdbcArtistDao;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Service artiste branché sur la base MySQL via JDBC
public class JdbcArtistService implements ArtistService {

    // DAO utilisé pour accéder à la table artists
    private final JdbcArtistDao artistDao = new JdbcArtistDao();

    @Override
    public List<Artist> getAllArtists() {
        // Délégation directe au DAO
        return artistDao.findAll();
    }

    @Override
    public Optional<Artist> getArtistByName(String name) {
        // Parcours de la liste pour trouver l'artiste par son nom
        List<Artist> tous = artistDao.findAll();

        for (Artist a : tous) {
            if (a.getName().equals(name)) {
                return Optional.of(a);
            }
        }

        return Optional.empty();
    }

    @Override
    public void createArtist(Artist artist) {
        artistDao.save(artist);
    }

    @Override
    public void updateArtist(Artist artist) {
        artistDao.update(artist);
    }

    @Override
    public void deleteArtist(String name) {
        artistDao.delete(name);
    }

    @Override
    public List<Discipline> getAllDisciplines() {
        List<Discipline> disciplines = new ArrayList<Discipline>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connexion à la base
            conn = ConnectionManager.getConnection();
            stmt = conn.prepareStatement("SELECT id, name FROM disciplines ORDER BY name");
            rs = stmt.executeQuery();

            // Parcours des résultats
            while (rs.next()) {
                disciplines.add(new Discipline(rs.getInt("id"), rs.getString("name")));
            }

        } catch (SQLException e) {
            System.err.println("Erreur getAllDisciplines : " + e.getMessage());
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

        return disciplines;
    }

    @Override
    public List<Artist> searchArtists(String query, String disciplineName, String city) {
        // Chargement de tous les artistes depuis la base
        List<Artist> tous = artistDao.findAll();
        List<Artist> resultats = new ArrayList<Artist>();

        for (Artist a : tous) {
            // Vérification du nom (recherche insensible à la casse)
            boolean nomOk = true;
            if (query != null && !query.isEmpty()) {
                if (!a.getName().toLowerCase().contains(query.toLowerCase())) {
                    nomOk = false;
                }
            }

            // Vérification de la ville
            boolean villeOk = true;
            if (city != null && !city.isEmpty()) {
                if (a.getCity() == null || !a.getCity().equalsIgnoreCase(city)) {
                    villeOk = false;
                }
            }

            // Vérification de la discipline
            boolean disciplineOk = true;
            if (disciplineName != null && !disciplineName.isEmpty()) {
                disciplineOk = false;
                for (Discipline d : a.getDisciplines()) {
                    if (d.getName().equals(disciplineName)) {
                        disciplineOk = true;
                        break;
                    }
                }
            }

            // L'artiste est retenu si tous les critères sont vérifiés
            if (nomOk && villeOk && disciplineOk) {
                resultats.add(a);
            }
        }

        return resultats;
    }
}
