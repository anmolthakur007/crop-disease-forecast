package com.cropsentinel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cropsentinel/main.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);
        stage.setTitle("Crop Sentinel - Disease Forecast");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}