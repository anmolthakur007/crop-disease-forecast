package com.cropsentinel;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.net.URI;
import java.net.http.*;
import java.util.Map;

public class StatsController {

    @FXML private Label totalLabel;
    @FXML private Label avgRiskLabel;
    @FXML private Label highestRiskLabel;
    @FXML private Label diseaseLabel;

    private static final String STATS_URL = "http://localhost:8080/api/stats";

    @FXML
    public void initialize() {
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(STATS_URL))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> stats = mapper.readValue(
                        response.body(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});

                Platform.runLater(() -> {
                    totalLabel.setText(String.valueOf(stats.get("totalForecasts")));
                    avgRiskLabel.setText(stats.get("averageRisk") + "%");
                    highestRiskLabel.setText(stats.get("highestRisk") + "%");
                    diseaseLabel.setText(String.valueOf(stats.get("mostCommonDisease")));
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}