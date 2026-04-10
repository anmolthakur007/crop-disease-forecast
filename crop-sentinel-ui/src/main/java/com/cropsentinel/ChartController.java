package com.cropsentinel;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import java.net.URI;
import java.net.http.*;
import java.util.*;

public class ChartController {

    @FXML private BarChart<String, Number> riskChart;

    private static final String HISTORY_URL = "http://localhost:8080/api/history";

    @FXML
    public void initialize() {
        if (riskChart == null) {
            System.err.println("ERROR: riskChart not injected from FXML!");
            showError("Chart Error", "riskChart component not initialized from FXML");
            return;
        }
        loadChart();
    }

    @FXML
    public void loadChart() {
        new Thread(() -> {
            try {
                System.out.println("Loading chart data from: " + HISTORY_URL);
                
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(HISTORY_URL))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    String errorMsg = "API returned status code: " + response.statusCode();
                    System.err.println(errorMsg);
                    showError("API Error", errorMsg);
                    return;
                }

                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> data = mapper.readValue(
                        response.body(),
                        mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

                if (data == null || data.isEmpty()) {
                    System.err.println("No data returned from API");
                    showError("No Data", "API returned empty data");
                    return;
                }

                System.out.println("Received " + data.size() + " records from API");

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Outbreak Risk %");

                int count = 1;
                for (Map<String, Object> record : data) {
                    try {
                        Object conditionObj = record.get("predictedCondition");
                        Object riskObj = record.get("finalOutbreakRisk");

                        if (conditionObj == null || riskObj == null) {
                            System.out.println("Skipping record with null values: " + record);
                            continue;
                        }

                        String condition = String.valueOf(conditionObj);
                        String label = "#" + count + " " +
                                condition.replace("___", "→")
                                        .substring(0, Math.min(15, condition.length()));
                        
                        double risk = ((Number) riskObj).doubleValue();
                        
                        series.getData().add(new XYChart.Data<>(label, risk));
                        count++;
                    } catch (Exception e) {
                        System.err.println("Error processing record " + count + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                final int totalCount = count - 1;
                Platform.runLater(() -> {
                    try {
                        if (riskChart != null) {
                            riskChart.getData().clear();
                            riskChart.getData().add(series);
                            System.out.println("Chart updated with " + totalCount + " entries");
                        } else {
                            System.err.println("ERROR: riskChart is null when updating chart!");
                        }
                    } catch (Exception e) {
                        System.err.println("Error updating chart UI: " + e.getMessage());
                        e.printStackTrace();
                        showError("Chart Update Error", "Failed to update chart: " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                System.err.println("Exception in loadChart: " + e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace();
                showError("Chart Loading Error", "Failed to load chart data: " + e.getMessage());
            }
        }).start();
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}