package org.example.main.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.example.main.models.Ad;
import org.example.main.utils.SessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
class IndexControllerTest {
    private IndexController controller;
    private SessionManager sessionManager;

    @Start
    void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/index.fxml"));
        Scene scene = new Scene(loader.load());
        controller = loader.getController();
        sessionManager = mock(SessionManager.class);
        controller.initialize();
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void testWelcomeText(FxRobot robot) {
        verifyThat("#welcomeText", hasText("Добро пожаловать в приложение!"));
    }

    @Test
    void testLoadAds(FxRobot robot) throws Exception {
        Ad ad = new Ad();
        ad.setAdId(1);
        ad.setTitle("Test Ad");
        ad.setPrice(100.0);
        ad.setStatus("active");

        InMemoryDatabase db = mock(InMemoryDatabase.class);
        when(db.loadAds()).thenReturn(Arrays.asList(ad));
        when(sessionManager.getLoggedInUserId()).thenReturn(2);

        controller.loadAds();

        ListView<Ad> adsList = robot.lookup("#adsList").queryAs(ListView.class);
        verifyThat(adsList, list -> list.getItems().size() == 1);
        verifyThat(adsList, list -> list.getItems().get(0).getTitle().equals("Test Ad"));
    }
}