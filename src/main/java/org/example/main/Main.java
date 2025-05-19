package org.example.main;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.main.utils.SceneManager;


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

    public static void main(String[] args) {
        launch(args);
    }
}