package com.cropsentinel;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;

public class PredictController {

    @FXML private TextField latField;
    @FXML private TextField lonField;
    @FXML private Label fileLabel;
    @FXML private Label riskLabel;
    @FXML private Label conditionLabel;
    @FXML private Label tempLabel;
    @FXML private Label humidityLabel;
    @FXML private Label confidenceLabel;
    @FXML private Label statusLabel;
    @FXML private VBox resultBox;

    private File selectedFile;
    private static final String SPRING_URL = "http://localhost:8080/api/forecast";

    @FXML
    public void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Leaf Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png")
        );
        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            fileLabel.setText(selectedFile.getName());
        }
    }

    @FXML
    public void getForecast() {
        if (selectedFile == null) {
            statusLabel.setText("Please select an image first.");
            return;
        }
        if (latField.getText().isEmpty() || lonField.getText().isEmpty()) {
            statusLabel.setText("Please enter latitude and longitude.");
            return;
        }

        statusLabel.setText("Analyzing... please wait.");

        // Run in background thread so UI doesn't freeze
        new Thread(() -> {
            try {
                String boundary = "----JavaBoundary" + System.currentTimeMillis();
                String lat = latField.getText();
                String lon = lonField.getText();

                // Build multipart body manually
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, "UTF-8"), true);

                // lat field
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"lat\"").append("\r\n\r\n");
                writer.append(lat).append("\r\n");

                // lon field
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"lon\"").append("\r\n\r\n");
                writer.append(lon).append("\r\n");

                // file field
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                        .append(selectedFile.getName()).append("\"").append("\r\n");
                writer.append("Content-Type: image/jpeg").append("\r\n\r\n");
                writer.flush();

                baos.write(Files.readAllBytes(selectedFile.toPath()));
                baos.write(("\r\n--" + boundary + "--\r\n").getBytes());

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SPRING_URL))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // Parse JSON response
                ObjectMapper mapper = new ObjectMapper();
                var result = mapper.readValue(response.body(), java.util.Map.class);

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    double risk = ((Number) result.get("final_outbreak_risk")).doubleValue();
                    String condition = (String) result.get("predicted_condition");
                    double temp = ((Number) result.get("forecasted_temp")).doubleValue();
                    double humidity = ((Number) result.get("forecasted_humidity")).doubleValue();
                    double confidence = ((Number) result.get("visual_confidence")).doubleValue();

                    // Color code the risk
                    if (risk > 75) {
                        riskLabel.setText("🔴 HIGH RISK: " + risk + "%");
                        riskLabel.setStyle("-fx-text-fill: red; -fx-font-size: 24; -fx-font-weight: bold;");
                    } else if (risk > 40) {
                        riskLabel.setText("🟡 MODERATE RISK: " + risk + "%");
                        riskLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 24; -fx-font-weight: bold;");
                    } else {
                        riskLabel.setText("🟢 LOW RISK: " + risk + "%");
                        riskLabel.setStyle("-fx-text-fill: green; -fx-font-size: 24; -fx-font-weight: bold;");
                    }

                    conditionLabel.setText("Condition: " + condition.replace("___", " → "));
                    tempLabel.setText("Temperature: " + temp + "°C");
                    humidityLabel.setText("Humidity: " + humidity + "%");
                    confidenceLabel.setText("Model Confidence: " + confidence + "%");

                    resultBox.setVisible(true);
                    statusLabel.setText("");
                });

            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
            }
        }).start();
    }
}