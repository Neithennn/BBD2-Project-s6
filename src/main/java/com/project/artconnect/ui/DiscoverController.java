package com.project.artconnect.ui;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ServiceProvider;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.util.ArrayList;
import java.util.List;

public class DiscoverController {
    @FXML
    private FlowPane discoverPane;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();
    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();
    private final ArtworkService artworkService = ServiceProvider.getArtworkService();

    @FXML
    public void initialize() {
        discoverPane.getChildren().clear();

        // Collect some exhibitions from galleries
        List<Exhibition> featuredExhibitions = new ArrayList<>();
        for (Gallery g : galleryService.getAllGalleries()) {
            featuredExhibitions.addAll(g.getExhibitions());
            if (featuredExhibitions.size() >= 3)
                break;
        }

        featuredExhibitions.stream().limit(3).forEach(this::addExhibitionCard);
        artworkService.getAllArtworks().stream().limit(3).forEach(this::addArtworkCard);
        workshopService.getAllWorkshops().stream().limit(3).forEach(this::addWorkshopCard);
    }

    private void addExhibitionCard(Exhibition e) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: #e3f2fd; -fx-border-color: #2196f3; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefWidth(250);
        card.getChildren().addAll(
                new Label("FEATURED EXHIBITION"),
                new Label(e.getTitle()) {
                    {
                        setStyle("-fx-font-weight: bold;");
                    }
                },
                new Label("Theme: " + e.getTheme()),
                new Label("Gallery: " + (e.getGallery() != null ? e.getGallery().getName() : "Unknown")),
                new Label("Dates: " + e.getStartDate() + " to " + e.getEndDate()));
        addDescription(card, e.getDescription());
        discoverPane.getChildren().add(card);
    }

    private void addArtworkCard(Artwork artwork) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: #fff8e1; -fx-border-color: #ffb300; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefWidth(250);
        card.getChildren().addAll(
                new Label("ARTWORK"),
                new Label(artwork.getTitle()) {
                    {
                        setStyle("-fx-font-weight: bold;");
                    }
                },
                new Label("Artist: " + (artwork.getArtist() != null ? artwork.getArtist().getName() : "Unknown")),
                new Label("Type: " + nullToText(artwork.getType())),
                new Label("Status: " + artwork.getStatus()),
                new Label("Price: " + artwork.getPrice()));
        addDescription(card, artwork.getDescription());
        discoverPane.getChildren().add(card);
    }

    private void addWorkshopCard(Workshop w) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: #f1f8e9; -fx-border-color: #4caf50; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefWidth(250);
        card.getChildren().addAll(
                new Label("UPCOMING WORKSHOP"),
                new Label(w.getTitle()) {
                    {
                        setStyle("-fx-font-weight: bold;");
                    }
                },
                new Label("Instructor: " + (w.getInstructor() != null ? w.getInstructor().getName() : "Unknown")),
                new Label("Date: " + w.getDate()),
                new Label("Capacity: " + w.getMaxParticipants()),
                new Label("Price: " + w.getPrice()));
        addDescription(card, w.getDescription());
        discoverPane.getChildren().add(card);
    }

    private void addDescription(VBox card, String description) {
        if (description == null || description.trim().isEmpty()) {
            return;
        }

        Label label = new Label(description);
        label.setWrapText(true);
        card.getChildren().add(label);
    }

    private String nullToText(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }
}
