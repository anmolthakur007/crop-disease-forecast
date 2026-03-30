package com.cropsentinel;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        showPredict();
    }

    @FXML
    public void showPredict() {
        loadView("predict.fxml");
    }

    @FXML
    public void showHistory() {
        loadView("history.fxml");
    }

    @FXML
    public void showChart() {
        loadView("chart.fxml");
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cropsentinel/" + fxml));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}