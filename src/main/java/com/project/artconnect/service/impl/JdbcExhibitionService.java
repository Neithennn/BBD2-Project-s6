package com.project.artconnect.service.impl;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.persistence.JdbcExhibitionDao;
import com.project.artconnect.service.ExhibitionService;

import java.util.List;
import java.util.Optional;

// Service exposition branche sur la base MySQL via JDBC
public class JdbcExhibitionService implements ExhibitionService {

    // DAO utilise pour acceder a la table exhibitions
    private final JdbcExhibitionDao exhibitionDao = new JdbcExhibitionDao();

    @Override
    public List<Exhibition> getAllExhibitions() {
        // Delegation directe au DAO
        return exhibitionDao.findAll();
    }

    @Override
    public Optional<Exhibition> getExhibitionByTitle(String title) {
        // Parcours de la liste pour trouver l'exposition par son titre
        List<Exhibition> toutes = exhibitionDao.findAll();

        for (Exhibition e : toutes) {
            if (e.getTitle().equals(title)) {
                return Optional.of(e);
            }
        }

        return Optional.empty();
    }

    @Override
    public void createExhibition(Exhibition exhibition) {
        exhibitionDao.save(exhibition);
    }

    @Override
    public void updateExhibition(Exhibition exhibition) {
        exhibitionDao.update(exhibition);
    }

    @Override
    public void deleteExhibition(String title) {
        exhibitionDao.delete(title);
    }
}
