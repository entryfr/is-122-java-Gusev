package org.example.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.main.utils.SceneManager;

import java.io.IOException;

public class Main extends Application {

    private static final SceneManager sceneManager = SceneManager.getInstance();

    @Override
    public void start(Stage primaryStage) {
        sceneManager.setPrimaryStage(primaryStage);

        primaryStage.setTitle("Приложение");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);

        try {
            sceneManager.showScene("index");
        } catch (Exception e) {
            System.err.println("Не удалось загрузить начальную сцену: " + e.getMessage());
            e.printStackTrace();
        }

        primaryStage.show();
    }

    /**
     * Метод для загрузки FXML файла и отображения его в новом окне.
     */
    public static void loadForm(Stage stage, String fxmlPath, String title) {
        try {
            var fxmlUrl = Main.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Ошибка загрузки FXML файла: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Получение экземпляра SceneManager.
     */
    public static SceneManager getSceneManager() {
        return sceneManager;
    }

    /**
     * Точка входа в приложение.
     */
    public static void main(String[] args) {
        launch(args);
    }
}