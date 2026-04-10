package com.cropsentinel;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

public class PredictController {

    @FXML private TextField latField;
    @FXML private TextField lonField;
    @FXML private TextField addressField;
    @FXML private Label addressStatus;
    @FXML private Label fileLabel;
    @FXML private Label riskLabel;
    @FXML private Label conditionLabel;
    @FXML private Label tempLabel;
    @FXML private Label humidityLabel;
    @FXML private Label confidenceLabel;
    @FXML private Label statusLabel;
    @FXML private VBox resultBox;
    @FXML private ImageView imagePreview;

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
            // Show image preview
            Image image = new Image(selectedFile.toURI().toString());
            imagePreview.setImage(image);
        }
    }

    @FXML
    public void lookupAddress() {
        String address = addressField.getText().trim();
        if (address.isEmpty()) {
            addressStatus.setText("Please enter an address.");
            return;
        }
        addressStatus.setText("Looking up...");

        new Thread(() -> {
            try {
                String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
                String url = "https://nominatim.openstreetmap.org/search?q="
                        + encoded + "&format=json&limit=1";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "CropSentinel/1.0")
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> results = mapper.readValue(
                        response.body(),
                        mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

                if (results.isEmpty()) {
                    Platform.runLater(() -> addressStatus.setText("Address not found. Try again."));
                    return;
                }

                String lat = String.valueOf(results.get(0).get("lat"));
                String lon = String.valueOf(results.get(0).get("lon"));
                String displayName = String.valueOf(results.get(0).get("display_name"));

                Platform.runLater(() -> {
                    latField.setText(lat.substring(0, Math.min(8, lat.length())));
                    lonField.setText(lon.substring(0, Math.min(8, lon.length())));
                    addressStatus.setText("✅ Found: " + displayName.substring(0,
                            Math.min(50, displayName.length())) + "...");
                });

            } catch (Exception e) {
                Platform.runLater(() -> addressStatus.setText("Error: " + e.getMessage()));
            }
        }).start();
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

        new Thread(() -> {
            try {
                String boundary = "----JavaBoundary" + System.currentTimeMillis();
                String lat = latField.getText();
                String lon = lonField.getText();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, "UTF-8"), true);

                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"lat\"").append("\r\n\r\n");
                writer.append(lat).append("\r\n");

                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"lon\"").append("\r\n\r\n");
                writer.append(lon).append("\r\n");

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

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                ObjectMapper mapper = new ObjectMapper();
                var result = mapper.readValue(response.body(), Map.class);

                Platform.runLater(() -> {
                    double risk = ((Number) result.get("final_outbreak_risk")).doubleValue();
                    String condition = (String) result.get("predicted_condition");
                    double temp = ((Number) result.get("forecasted_temp")).doubleValue();
                    double humidity = ((Number) result.get("forecasted_humidity")).doubleValue();
                    double confidence = ((Number) result.get("visual_confidence")).doubleValue();

                    if (risk > 75) {
                        riskLabel.setText("🔴 HIGH RISK: " + risk + "%");
                        riskLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16; -fx-font-weight: bold;");                  } else if (risk > 40) {
                        riskLabel.setText("🟡 MODERATE: " + risk + "%");
                        riskLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 20; -fx-font-weight: bold;");
                    } else {
                        riskLabel.setText("🟢 LOW RISK: " + risk + "%");
                        riskLabel.setStyle("-fx-text-fill: green; -fx-font-size: 20; -fx-font-weight: bold;");
                    }

                    conditionLabel.setText("📋 " + condition.replace("___", " → "));
                    tempLabel.setText("🌡 " + temp + "°C");
                    humidityLabel.setText("💧 " + humidity + "%");
                    confidenceLabel.setText("🎯 Confidence: " + confidence + "%");

                    resultBox.setVisible(true);
                    statusLabel.setText("");

                    if (risk > 75) {
                        showDiseaseAlert(condition, risk);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void showDiseaseAlert(String condition, double risk) {
        new Thread(() -> {
            try {
                String encoded = condition.replace(" ", "%20");
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/disease-info/" + encoded))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> info = mapper.readValue(response.body(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("⚠️ Disease Alert");
                    alert.setHeaderText("HIGH RISK DETECTED: " + info.get("name")
                            + " (" + risk + "%)");

                    String content = "SYMPTOMS:\n" + info.get("symptoms")
                            + "\n\nRECOMMENDED TREATMENT:\n" + info.get("treatment");

                    alert.setContentText(content);
                    alert.getDialogPane().setPrefWidth(500);
                    alert.showAndWait();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}