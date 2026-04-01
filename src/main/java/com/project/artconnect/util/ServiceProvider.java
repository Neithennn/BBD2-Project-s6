package com.project.artconnect.util;

import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.service.impl.JdbcArtistService;
import com.project.artconnect.service.impl.JdbcArtworkService;
import com.project.artconnect.service.impl.JdbcCommunityService;
import com.project.artconnect.service.impl.JdbcGalleryService;
import com.project.artconnect.service.impl.JdbcWorkshopService;

// Fournisseur de services — utilise les implémentations JDBC
public class ServiceProvider {

    // Initialisation des services avec les DAOs JDBC
    private static final ArtistService    artistService    = new JdbcArtistService();
    private static final ArtworkService   artworkService   = new JdbcArtworkService();
    private static final GalleryService   galleryService   = new JdbcGalleryService();
    private static final WorkshopService  workshopService  = new JdbcWorkshopService();
    private static final CommunityService communityService = new JdbcCommunityService();

    public static ArtistService getArtistService() {
        return artistService;
    }

    public static ArtworkService getArtworkService() {
        return artworkService;
    }

    public static GalleryService getGalleryService() {
        return galleryService;
    }

    public static WorkshopService getWorkshopService() {
        return workshopService;
    }

    public static CommunityService getCommunityService() {
        return communityService;
    }
}
