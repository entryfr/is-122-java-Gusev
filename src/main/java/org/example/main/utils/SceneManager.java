package org.example.main.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {
    private static SceneManager instance; // Единственный экземпляр класса
    private Stage primaryStage;
    private final Map<String, Object> parameters = new HashMap<>(); // Параметры для передачи между сценами
    private final Map<String, Object> controllers = new HashMap<>(); // Карта для хранения контроллеров

    /**
     * Приватный конструктор для предотвращения создания экземпляров извне.
     */
    private SceneManager() {}

    /**
     * Получение единственного экземпляра SceneManager.
     */
    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    /**
     * Устанавливает основную сцену (Stage).
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Регистрация контроллера для сцены.
     */
    public void registerController(String sceneName, Object controller) {
        controllers.put(sceneName, controller);
        System.out.println("Контроллер зарегистрирован: " + sceneName);
    }

    /**
     * Показывает сцену без параметров.
     */
    public void showScene(String fxmlFileName) {
        try {
            URL fxmlUrl = getClass().getResource("/org/example/main/" + fxmlFileName + ".fxml");
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: " + fxmlFileName);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Регистрируем контроллер
            Object controller = loader.getController();
            if (controller != null) {
                registerController(fxmlFileName, controller);
            }

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Не удалось загрузить сцену: " + fxmlFileName);
        }
    }

    /**
     * Показывает сцену с параметрами.
     */
    public void showSceneWithParameters(String fxmlFileName, String paramName, Object paramValue) {
        try {
            if (paramName != null && paramValue != null) {
                parameters.put(paramName, paramValue);
            }

            URL fxmlUrl = getClass().getResource("/org/example/main/" + fxmlFileName + ".fxml");
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: " + fxmlFileName);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller != null) {
                registerController(fxmlFileName, controller);
            }

            if (controller instanceof Parameterizable) {
                ((Parameterizable) controller).setParameters(parameters);
            }

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

            clearParameters();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Не удалось загрузить сцену: " + fxmlFileName);
        }
    }

    /**
     * Получает текущий контроллер по имени сцены.
     */
    public Object getCurrentController(String sceneName) {
        Object controller = controllers.get(sceneName);
        if (controller == null) {
            System.err.println("Контроллер для сцены '" + sceneName + "' не найден.");
        }
        return controller;
    }

    /**
     * Получает переданный параметр.
     */
    public <T> T getPassedParameter(String paramName, T defaultValue) {
        return (T) parameters.getOrDefault(paramName, defaultValue);
    }

    /**
     * Очищает все сохраненные параметры.
     */
    public void clearParameters() {
        parameters.clear();
        System.out.println("Параметры очищены.");
    }

    /**
     * Очищает все зарегистрированные контроллеры.
     */
    public void clearControllers() {
        controllers.clear();
        System.out.println("Все контроллеры очищены.");
    }

    /**
     * Интерфейс для контроллеров, поддерживающих передачу параметров.
     */
    public interface Parameterizable {
        void setParameters(Map<String, Object> parameters);
    }
}