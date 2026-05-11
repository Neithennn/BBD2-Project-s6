package com.project.artconnect.ui;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.ExhibitionService;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.List;

public class ExhibitionController {
    @FXML
    private TableView<Exhibition> exhibitionTable;
    @FXML
    private TableColumn<Exhibition, String> titleColumn;
    @FXML
    private TableColumn<Exhibition, LocalDate> dateColumn;
    @FXML
    private TableColumn<Exhibition, String> themeColumn;
    @FXML
    private TableColumn<Exhibition, String> galleryColumn;
    @FXML
    private TableColumn<Exhibition, LocalDate> endDateColumn;
    @FXML
    private TableColumn<Exhibition, String> curatorColumn;
    @FXML
    private TextField titleField;
    @FXML
    private DatePicker startDateInput;
    @FXML
    private DatePicker endDateInput;
    @FXML
    private TextField themeField;
    @FXML
    private TextField curatorField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ComboBox<Gallery> galleryInput;

    // On utilise maintenant ExhibitionService (direct BD) au lieu de passer par les galeries
    private final ExhibitionService exhibitionService = ServiceProvider.getExhibitionService();
    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        themeColumn.setCellValueFactory(new PropertyValueFactory<>("theme"));
        curatorColumn.setCellValueFactory(new PropertyValueFactory<>("curatorName"));

        galleryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getGallery() != null ? cellData.getValue().getGallery().getName() : "Unknown"));

        reloadGalleries();
        exhibitionTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selected) -> populateForm(selected));

        refreshData();
    }

    @FXML
    private void handleCreate() {
        try {
            Exhibition exhibition = readForm(null);
            exhibitionService.createExhibition(exhibition);
            refreshData();
            selectExhibition(exhibition.getTitle());
            showInfo("Exposition creee", "L'exposition a ete enregistree dans la base.");
        } catch (RuntimeException e) {
            showError("Creation impossible", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Exhibition selected = exhibitionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Aucune selection", "Selectionnez une exposition a modifier.");
            return;
        }

        try {
            Exhibition exhibition = readForm(selected.getId());
            exhibitionService.updateExhibition(exhibition);
            refreshData();
            selectExhibition(exhibition.getTitle());
            showInfo("Exposition mise a jour", "Les modifications ont ete enregistrees.");
        } catch (RuntimeException e) {
            showError("Modification impossible", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Exhibition selected = exhibitionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Aucune selection", "Selectionnez une exposition a supprimer.");
            return;
        }

        try {
            exhibitionService.deleteExhibition(selected.getTitle());
            refreshData();
            clearForm();
            showInfo("Exposition supprimee", "L'exposition a ete supprimee.");
        } catch (RuntimeException e) {
            showError("Suppression impossible", e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        exhibitionTable.getSelectionModel().clearSelection();
        clearForm();
    }

    private void refreshData() {
        // Recuperation directe des expositions depuis la base
        List<Exhibition> all = exhibitionService.getAllExhibitions();
        exhibitionTable.setItems(FXCollections.observableArrayList(all));
    }

    private void reloadGalleries() {
        galleryInput.setItems(FXCollections.observableArrayList(galleryService.getAllGalleries()));
    }

    private Exhibition readForm(Integer id) {
        Gallery gallery = galleryInput.getValue();
        if (gallery == null) {
            throw new IllegalArgumentException("Une galerie doit etre selectionnee.");
        }

        LocalDate startDate = startDateInput.getValue();
        LocalDate endDate = endDateInput.getValue();
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Les dates de debut et de fin sont obligatoires.");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("La date de fin doit etre apres la date de debut.");
        }

        Exhibition exhibition = new Exhibition();
        exhibition.setId(id);
        exhibition.setTitle(required(titleField, "Le titre est obligatoire."));
        exhibition.setStartDate(startDate);
        exhibition.setEndDate(endDate);
        exhibition.setTheme(optional(themeField));
        exhibition.setCuratorName(optional(curatorField));
        exhibition.setDescription(optional(descriptionField));
        exhibition.setGallery(gallery);
        return exhibition;
    }

    private void populateForm(Exhibition exhibition) {
        if (exhibition == null) {
            return;
        }

        titleField.setText(nullToEmpty(exhibition.getTitle()));
        startDateInput.setValue(exhibition.getStartDate());
        endDateInput.setValue(exhibition.getEndDate());
        themeField.setText(nullToEmpty(exhibition.getTheme()));
        curatorField.setText(nullToEmpty(exhibition.getCuratorName()));
        descriptionField.setText(nullToEmpty(exhibition.getDescription()));
        selectGallery(exhibition.getGallery());
    }

    private void clearForm() {
        titleField.clear();
        startDateInput.setValue(null);
        endDateInput.setValue(null);
        themeField.clear();
        curatorField.clear();
        descriptionField.clear();
        galleryInput.setValue(null);
    }

    private void selectGallery(Gallery gallery) {
        if (gallery == null) {
            galleryInput.setValue(null);
            return;
        }

        galleryInput.getItems().stream()
                .filter(item -> item.getName().equals(gallery.getName()))
                .findFirst()
                .ifPresent(galleryInput::setValue);
    }

    private void selectExhibition(String title) {
        exhibitionTable.getItems().stream()
                .filter(exhibition -> exhibition.getTitle().equals(title))
                .findFirst()
                .ifPresent(exhibition -> exhibitionTable.getSelectionModel().select(exhibition));
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
