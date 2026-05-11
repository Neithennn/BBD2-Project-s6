package com.project.artconnect.service;

import com.project.artconnect.model.Exhibition;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des expositions.
 */
public interface ExhibitionService {

    // Retourne toutes les expositions de la base
    List<Exhibition> getAllExhibitions();

    // Retourne une exposition par son titre
    Optional<Exhibition> getExhibitionByTitle(String title);

    // Cree une nouvelle exposition
    void createExhibition(Exhibition exhibition);

    // Met a jour une exposition existante
    void updateExhibition(Exhibition exhibition);

    // Supprime une exposition par son titre
    void deleteExhibition(String title);
}
