package com.project.artconnect.ui;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDateTime;

public class WorkshopController {
    @FXML
    private TableView<Workshop> workshopTable;
    @FXML
    private TableColumn<Workshop, String> titleColumn;
    @FXML
    private TableColumn<Workshop, LocalDateTime> dateColumn;
    @FXML
    private TableColumn<Workshop, String> instructorColumn;
    @FXML
    private TableColumn<Workshop, Double> priceColumn;
    @FXML
    private TableColumn<Workshop, String> levelColumn;
    @FXML
    private TableColumn<Workshop, Integer> durationColumn;
    @FXML
    private TableColumn<Workshop, Integer> capacityColumn;
    @FXML
    private TableColumn<Workshop, String> locationColumn;
    @FXML
    private ComboBox<CommunityMember> memberInput;

    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();
    private final CommunityService communityService = ServiceProvider.getCommunityService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("maxParticipants"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));

        instructorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getInstructor() != null ? cellData.getValue().getInstructor().getName()
                        : "Unknown"));

        memberInput.setItems(FXCollections.observableArrayList(communityService.getAllMembers()));
        workshopTable.setItems(FXCollections.observableArrayList(workshopService.getAllWorkshops()));
    }

    @FXML
    private void handleBookWorkshop() {
        Workshop workshop = workshopTable.getSelectionModel().getSelectedItem();
        CommunityMember member = memberInput.getValue();

        if (workshop == null) {
            showError("Aucune selection", "Selectionnez un atelier.");
            return;
        }
        if (member == null) {
            showError("Aucun membre", "Selectionnez un membre de la communaute.");
            return;
        }

        try {
            workshopService.bookWorkshop(workshop, member);
            showInfo("Reservation creee", "Le membre est inscrit a l'atelier.");
        } catch (RuntimeException e) {
            showError("Reservation impossible", e.getMessage());
        }
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
