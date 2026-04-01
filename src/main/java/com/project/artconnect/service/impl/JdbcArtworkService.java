package com.project.artconnect.service.impl;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.persistence.JdbcArtworkDao;
import com.project.artconnect.service.ArtworkService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Service oeuvres branché sur la base MySQL via JDBC
public class JdbcArtworkService implements ArtworkService {

    // DAO utilisé pour accéder à la table artworks
    private final JdbcArtworkDao artworkDao = new JdbcArtworkDao();

    @Override
    public List<Artwork> getAllArtworks() {
        // Délégation directe au DAO
        return artworkDao.findAll();
    }

    @Override
    public Optional<Artwork> getArtworkByTitle(String title) {
        // Parcours de la liste pour trouver l'oeuvre par son titre
        List<Artwork> toutes = artworkDao.findAll();

        for (Artwork a : toutes) {
            if (a.getTitle().equals(title)) {
                return Optional.of(a);
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Artwork> getArtworksByArtist(Artist artist) {
        if (artist == null) {
            return new ArrayList<Artwork>();
        }
        // Le DAO cherche directement par nom d'artiste
        return artworkDao.findByArtistName(artist.getName());
    }

    @Override
    public void createArtwork(Artwork artwork) {
        artworkDao.save(artwork);
    }

    @Override
    public void updateArtwork(Artwork artwork) {
        artworkDao.update(artwork);
    }

    @Override
    public void deleteArtwork(String title) {
        artworkDao.delete(title);
    }
}
