package com.cropsentinel;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.net.URI;
import java.net.http.*;
import java.util.*;

public class HistoryController {

    @FXML private TableView<Map<String, Object>> historyTable;
    @FXML private TableColumn<Map<String, Object>, String> conditionCol;
    @FXML private TableColumn<Map<String, Object>, String> riskCol;
    @FXML private TableColumn<Map<String, Object>, String> confidenceCol;
    @FXML private TableColumn<Map<String, Object>, String> tempCol;
    @FXML private TableColumn<Map<String, Object>, String> humidityCol;
    @FXML private TableColumn<Map<String, Object>, String> dateCol;

    private static final String HISTORY_URL = "http://localhost:8080/api/history";

    @FXML
    public void initialize() {
        conditionCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().get("predictedCondition")).replace("___", " → ")));
        riskCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().get("finalOutbreakRisk")) + "%"));
        confidenceCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().get("visualConfidence")) + "%"));
        tempCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().get("forecastedTemp")) + "°C"));
        humidityCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().get("forecastedHumidity")) + "%"));
        dateCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().get("createdAt"))));

        loadHistory();
    }

    @FXML
    public void loadHistory() {
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(HISTORY_URL))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> data = mapper.readValue(
                        response.body(),
                        mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

                ObservableList<Map<String, Object>> items =
                        FXCollections.observableArrayList(data);

                javafx.application.Platform.runLater(() ->
                        historyTable.setItems(items));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}