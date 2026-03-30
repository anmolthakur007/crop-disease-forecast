package com.cropsentinel;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import java.net.URI;
import java.net.http.*;
import java.util.*;

public class ChartController {

    @FXML private BarChart<String, Number> riskChart;

    private static final String HISTORY_URL = "http://localhost:8080/api/history";

    @FXML
    public void initialize() {
        loadChart();
    }

    @FXML
    public void loadChart() {
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

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Outbreak Risk %");

                int count = 1;
                for (Map<String, Object> record : data) {
                    String label = "#" + count + " " +
                            String.valueOf(record.get("predictedCondition"))
                                    .replace("___", "→")
                                    .substring(0, Math.min(15,
                                            String.valueOf(record.get("predictedCondition")).length()));
                    double risk = ((Number) record.get("finalOutbreakRisk")).doubleValue();
                    series.getData().add(new XYChart.Data<>(label, risk));
                    count++;
                }

                Platform.runLater(() -> {
                    riskChart.getData().clear();
                    riskChart.getData().add(series);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}