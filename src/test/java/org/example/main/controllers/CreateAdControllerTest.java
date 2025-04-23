package org.example.main.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.example.main.utils.SessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.ComboBoxMatchers.hasItems;

@ExtendWith(ApplicationExtension.class)
class CreateAdControllerTest {
    private CreateAdController controller;
    private InMemoryDatabase db;
    private SessionManager sessionManager;

    @Start
    void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/create_ad.fxml"));
        Scene scene = new Scene(loader.load());
        controller = loader.getController();
        db = mock(InMemoryDatabase.class);
        sessionManager = mock(SessionManager.class);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void testLoadCategories(FxRobot robot) throws Exception {
        when(db.loadCategories()).thenReturn(Arrays.asList("Электроника", "Бытовая техника"));
        controller.initialize();

        ComboBox<String> categoryComboBox = robot.lookup("#categoryComboBox").queryAs(ComboBox.class);
        verifyThat(categoryComboBox, hasItems(2));
        verifyThat(categoryComboBox, combo -> combo.getItems().contains("Электроника"));
    }

    @Test
    void testCreateAdWithoutLogin(FxRobot robot) {
        when(sessionManager.isLoggedIn()).thenReturn(false);

        robot.clickOn("#titleField").write("Test Ad");
        robot.clickOn("#priceField").write("100.0");
        robot.clickOn("#descriptionField").write("Description");
        robot.clickOn("#cityComboBox").write("Москва");
        robot.clickOn("#createAdButton");

        // Проверяем, что отображается сообщение об ошибке
        verifyThat(".dialog-pane", node -> node.isVisible());
    }
}