package org.example.main.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneManager {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void showScene(String fxmlFileName) {
        try {
            if (primaryStage == null) {
                throw new IllegalStateException("Primary stage is not set. Call setPrimaryStage() first.");
            }

            // Формируем путь к FXML-файлу
            URL fxmlUrl = SceneManager.class.getResource("/org/example/main/" + fxmlFileName + ".fxml");
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: " + fxmlFileName);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Не удалось загрузить сцену: " + fxmlFileName);
        }
    }
}