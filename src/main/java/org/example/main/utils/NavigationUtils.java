package org.example.main.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationUtils {

    public static void navigateToMainPage(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationUtils.class.getResource("/org/example/main/index.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Главная страница");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}