package com.project.artconnect.ui;

import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class GalleryController {
    @FXML
    private TableView<Gallery> galleryTable;
    @FXML
    private TableColumn<Gallery, String> nameColumn;
    @FXML
    private TableColumn<Gallery, String> addressColumn;
    @FXML
    private TableColumn<Gallery, String> ownerColumn;
    @FXML
    private TableColumn<Gallery, String> hoursColumn;
    @FXML
    private TableColumn<Gallery, String> phoneColumn;
    @FXML
    private TableColumn<Gallery, Double> ratingColumn;
    @FXML
    private TableColumn<Gallery, String> websiteColumn;
    @FXML
    private TableColumn<Gallery, Number> exhibitionsColumn;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        hoursColumn.setCellValueFactory(new PropertyValueFactory<>("openingHours"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("contactPhone"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        websiteColumn.setCellValueFactory(new PropertyValueFactory<>("website"));
        exhibitionsColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                cellData.getValue().getExhibitions() == null ? 0 : cellData.getValue().getExhibitions().size()));

        galleryTable.setItems(FXCollections.observableArrayList(galleryService.getAllGalleries()));
    }
}
