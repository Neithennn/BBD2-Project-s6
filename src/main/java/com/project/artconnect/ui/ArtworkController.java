package com.project.artconnect.ui;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Artist;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.cell.PropertyValueFactory;

public class ArtworkController {
    @FXML
    private TableView<Artwork> artworkTable;
    @FXML
    private TableColumn<Artwork, String> titleColumn;
    @FXML
    private TableColumn<Artwork, String> typeColumn;
    @FXML
    private TableColumn<Artwork, Double> priceColumn;
    @FXML
    private TableColumn<Artwork, String> statusColumn;
    @FXML
    private TableColumn<Artwork, String> artistColumn;
    @FXML
    private TableColumn<Artwork, Integer> yearColumn;
    @FXML
    private TableColumn<Artwork, String> mediumColumn;
    @FXML
    private TableColumn<Artwork, String> dimensionsColumn;
    @FXML
    private TextField titleField;
    @FXML
    private TextField creationYearField;
    @FXML
    private TextField typeField;
    @FXML
    private TextField mediumField;
    @FXML
    private TextField dimensionsField;
    @FXML
    private TextField priceField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ComboBox<Artwork.Status> statusInput;
    @FXML
    private ComboBox<Artist> artistInput;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("creationYear"));
        mediumColumn.setCellValueFactory(new PropertyValueFactory<>("medium"));
        dimensionsColumn.setCellValueFactory(new PropertyValueFactory<>("dimensions"));

        artistColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getArtist() != null ? cellData.getValue().getArtist().getName() : "Unknown"));

        statusInput.setItems(FXCollections.observableArrayList(Artwork.Status.values()));
        statusInput.setValue(Artwork.Status.FOR_SALE);
        reloadArtists();
        artworkTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selected) -> populateForm(selected));

        refreshTable();
    }

    @FXML
    private void handleCreate() {
        try {
            Artwork artwork = readForm(null);
            artworkService.createArtwork(artwork);
            refreshTable();
            selectArtwork(artwork.getTitle());
            showInfo("Oeuvre creee", "L'oeuvre a ete enregistree dans la base.");
        } catch (RuntimeException e) {
            showError("Creation impossible", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Aucune selection", "Selectionnez une oeuvre a modifier.");
            return;
        }

        try {
            Artwork artwork = readForm(selected.getId());
            artworkService.updateArtwork(artwork);
            refreshTable();
            selectArtwork(artwork.getTitle());
            showInfo("Oeuvre mise a jour", "Les modifications ont ete enregistrees.");
        } catch (RuntimeException e) {
            showError("Modification impossible", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Aucune selection", "Selectionnez une oeuvre a supprimer.");
            return;
        }

        try {
            artworkService.deleteArtwork(selected.getTitle());
            refreshTable();
            clearForm();
            showInfo("Oeuvre supprimee", "L'oeuvre a ete supprimee.");
        } catch (RuntimeException e) {
            showError("Suppression impossible", e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        artworkTable.getSelectionModel().clearSelection();
        clearForm();
    }

    private void refreshTable() {
        artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks()));
    }

    private void reloadArtists() {
        artistInput.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
    }

    private Artwork readForm(Integer id) {
        Artist artist = artistInput.getValue();
        if (artist == null) {
            throw new IllegalArgumentException("Un artiste doit etre selectionne.");
        }

        Integer creationYear = parseOptionalInteger(creationYearField, "L'annee de creation doit etre un nombre.");
        if (creationYear != null && (creationYear < 1000 || creationYear > 2100)) {
            throw new IllegalArgumentException("L'annee de creation doit etre comprise entre 1000 et 2100.");
        }

        double price = parseOptionalDouble(priceField, "Le prix doit etre un nombre.");
        if (price < 0) {
            throw new IllegalArgumentException("Le prix ne peut pas etre negatif.");
        }

        Artwork artwork = new Artwork();
        artwork.setId(id);
        artwork.setTitle(required(titleField, "Le titre est obligatoire."));
        artwork.setCreationYear(creationYear);
        artwork.setType(optional(typeField));
        artwork.setMedium(optional(mediumField));
        artwork.setDimensions(optional(dimensionsField));
        artwork.setDescription(optional(descriptionField));
        artwork.setPrice(price);
        artwork.setStatus(statusInput.getValue() == null ? Artwork.Status.FOR_SALE : statusInput.getValue());
        artwork.setArtist(artist);
        return artwork;
    }

    private void populateForm(Artwork artwork) {
        if (artwork == null) {
            return;
        }

        titleField.setText(nullToEmpty(artwork.getTitle()));
        creationYearField.setText(artwork.getCreationYear() == null ? "" : artwork.getCreationYear().toString());
        typeField.setText(nullToEmpty(artwork.getType()));
        mediumField.setText(nullToEmpty(artwork.getMedium()));
        dimensionsField.setText(nullToEmpty(artwork.getDimensions()));
        descriptionField.setText(nullToEmpty(artwork.getDescription()));
        priceField.setText(Double.toString(artwork.getPrice()));
        statusInput.setValue(artwork.getStatus() == null ? Artwork.Status.FOR_SALE : artwork.getStatus());
        selectArtist(artwork.getArtist());
    }

    private void clearForm() {
        titleField.clear();
        creationYearField.clear();
        typeField.clear();
        mediumField.clear();
        dimensionsField.clear();
        priceField.clear();
        descriptionField.clear();
        statusInput.setValue(Artwork.Status.FOR_SALE);
        artistInput.setValue(null);
    }

    private void selectArtist(Artist artist) {
        if (artist == null) {
            artistInput.setValue(null);
            return;
        }

        artistInput.getItems().stream()
                .filter(item -> item.getName().equals(artist.getName()))
                .findFirst()
                .ifPresent(artistInput::setValue);
    }

    private void selectArtwork(String title) {
        artworkTable.getItems().stream()
                .filter(artwork -> artwork.getTitle().equals(title))
                .findFirst()
                .ifPresent(artwork -> artworkTable.getSelectionModel().select(artwork));
    }

    private Integer parseOptionalInteger(TextField field, String errorMessage) {
        String value = optional(field);
        if (value == null) {
            return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private double parseOptionalDouble(TextField field, String errorMessage) {
        String value = optional(field);
        if (value == null) {
            return 0.0;
        }

        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private String required(TextInputControl field, String errorMessage) {
        String value = optional(field);
        if (value == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value;
    }

    private String optional(TextInputControl field) {
        String value = field.getText();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
