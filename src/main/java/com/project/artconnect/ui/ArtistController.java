package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArtistController {
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Discipline> disciplineFilter;
    @FXML
    private TableView<Artist> artistTable;
    @FXML
    private TableColumn<Artist, String> nameColumn;
    @FXML
    private TableColumn<Artist, String> cityColumn;
    @FXML
    private TableColumn<Artist, String> emailColumn;
    @FXML
    private TableColumn<Artist, Integer> yearColumn;
    @FXML
    private TableColumn<Artist, String> disciplinesColumn;
    @FXML
    private TableColumn<Artist, String> activeColumn;
    @FXML
    private TextField nameField;
    @FXML
    private TextArea bioField;
    @FXML
    private TextField birthYearField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField websiteField;
    @FXML
    private TextField socialMediaField;
    @FXML
    private TextField disciplinesField;
    @FXML
    private CheckBox activeCheckBox;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));
        disciplinesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                formatDisciplines(cellData.getValue().getDisciplines())));
        activeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().isActive() ? "Actif" : "Inactif"));

        activeCheckBox.setSelected(true);
        artistTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selected) -> populateForm(selected));

        reloadDisciplines();
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        Discipline d = disciplineFilter.getValue();
        String dName = (d != null) ? d.getName() : null;
        artistTable.setItems(FXCollections.observableArrayList(artistService.searchArtists(query, dName, null)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        disciplineFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleCreate() {
        try {
            Artist artist = readForm(null);
            artistService.createArtist(artist);
            reloadDisciplines();
            refreshTable();
            selectArtist(artist.getName());
            showInfo("Artiste cree", "L'artiste a ete enregistre dans la base.");
        } catch (RuntimeException e) {
            showError("Creation impossible", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Aucune selection", "Selectionnez un artiste a modifier.");
            return;
        }

        try {
            Artist artist = readForm(selected.getId());
            artistService.updateArtist(artist);
            reloadDisciplines();
            refreshTable();
            selectArtist(artist.getName());
            showInfo("Artiste mis a jour", "Les modifications ont ete enregistrees.");
        } catch (RuntimeException e) {
            showError("Modification impossible", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Aucune selection", "Selectionnez un artiste a supprimer.");
            return;
        }

        try {
            artistService.deleteArtist(selected.getName());
            refreshTable();
            clearForm();
            showInfo("Artiste supprime", "L'artiste et ses donnees liees ont ete supprimes.");
        } catch (RuntimeException e) {
            showError("Suppression impossible", e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        artistTable.getSelectionModel().clearSelection();
        clearForm();
    }

    private void refreshTable() {
        artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
    }

    private void reloadDisciplines() {
        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
    }

    private Artist readForm(Integer id) {
        String name = required(nameField, "Le nom est obligatoire.");
        Integer birthYear = parseOptionalInteger(birthYearField, "L'annee de naissance doit etre un nombre.");
        if (birthYear != null && (birthYear < 1000 || birthYear > 2100)) {
            throw new IllegalArgumentException("L'annee de naissance doit etre comprise entre 1000 et 2100.");
        }

        String email = optional(emailField);
        if (email != null && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("L'adresse email n'est pas valide.");
        }

        Artist artist = new Artist();
        artist.setId(id);
        artist.setName(name);
        artist.setBio(optional(bioField));
        artist.setBirthYear(birthYear);
        artist.setContactEmail(email);
        artist.setPhone(optional(phoneField));
        artist.setCity(optional(cityField));
        artist.setWebsite(optional(websiteField));
        artist.setSocialMedia(optional(socialMediaField));
        artist.setActive(activeCheckBox.isSelected());
        artist.setDisciplines(parseDisciplines(disciplinesField.getText()));
        return artist;
    }

    private void populateForm(Artist artist) {
        if (artist == null) {
            return;
        }

        nameField.setText(nullToEmpty(artist.getName()));
        bioField.setText(nullToEmpty(artist.getBio()));
        birthYearField.setText(artist.getBirthYear() == null ? "" : artist.getBirthYear().toString());
        emailField.setText(nullToEmpty(artist.getContactEmail()));
        phoneField.setText(nullToEmpty(artist.getPhone()));
        cityField.setText(nullToEmpty(artist.getCity()));
        websiteField.setText(nullToEmpty(artist.getWebsite()));
        socialMediaField.setText(nullToEmpty(artist.getSocialMedia()));
        disciplinesField.setText(formatDisciplines(artist.getDisciplines()));
        activeCheckBox.setSelected(artist.isActive());
    }

    private void clearForm() {
        nameField.clear();
        bioField.clear();
        birthYearField.clear();
        emailField.clear();
        phoneField.clear();
        cityField.clear();
        websiteField.clear();
        socialMediaField.clear();
        disciplinesField.clear();
        activeCheckBox.setSelected(true);
    }

    private void selectArtist(String name) {
        artistTable.getItems().stream()
                .filter(artist -> artist.getName().equals(name))
                .findFirst()
                .ifPresent(artist -> artistTable.getSelectionModel().select(artist));
    }

    private List<Discipline> parseDisciplines(String value) {
        if (value == null || value.trim().isEmpty()) {
            return FXCollections.observableArrayList();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .distinct()
                .map(Discipline::new)
                .collect(Collectors.toList());
    }

    private String formatDisciplines(List<Discipline> disciplines) {
        if (disciplines == null || disciplines.isEmpty()) {
            return "";
        }

        return disciplines.stream()
                .map(Discipline::getName)
                .collect(Collectors.joining(", "));
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
