package com.project.artconnect.service.impl;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.persistence.JdbcExhibitionDao;
import com.project.artconnect.persistence.JdbcGalleryDao;
import com.project.artconnect.service.GalleryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Service galeries branché sur la base MySQL via JDBC
public class JdbcGalleryService implements GalleryService {

    // DAOs pour les galeries et les expositions
    private final JdbcGalleryDao galleryDao = new JdbcGalleryDao();
    private final JdbcExhibitionDao exhibitionDao = new JdbcExhibitionDao();

    @Override
    public List<Gallery> getAllGalleries() {
        // Délégation directe au DAO
        return galleryDao.findAll();
    }

    @Override
    public Optional<Gallery> getGalleryByName(String name) {
        // Parcours de la liste pour trouver la galerie par son nom
        List<Gallery> toutes = galleryDao.findAll();

        for (Gallery g : toutes) {
            if (g.getName().equals(name)) {
                return Optional.of(g);
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Exhibition> getExhibitionsByGallery(Gallery gallery) {
        if (gallery == null) {
            return new ArrayList<Exhibition>();
        }

        // Chargement de toutes les expositions, puis filtre par nom de galerie
        List<Exhibition> toutes = exhibitionDao.findAll();
        List<Exhibition> resultats = new ArrayList<Exhibition>();

        for (Exhibition e : toutes) {
            if (e.getGallery() != null && e.getGallery().getName().equals(gallery.getName())) {
                resultats.add(e);
            }
        }

        return resultats;
    }
}
