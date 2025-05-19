package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.main.models.Ad;
import org.example.main.utils.AuthObserver;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;

import java.io.IOException;
import java.util.List;

public class IndexController implements IndexControllerInterface, AuthObserver {

    @FXML
    private Button adminButton;
    @FXML
    public Label welcomeText;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Ad> adsList;

    @FXML
    private HBox authBlock;

    @FXML
    private HBox userBlock;

    @FXML
    private Label usernameLabel;

    private final SceneManager sceneManager = SceneManager.getInstance();
    private final InMemoryDatabase inMemoryDatabase = InMemoryDatabase.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @Override
    @FXML
    public void initialize() {
        welcomeText.setText("Добро пожаловать в приложение!");
        sessionManager.addObserver(this);
        updateUIBasedOnAuthStatus();

        // Настраиваем отображение элементов в списке объявлений
        adsList.setCellFactory(param -> new ListCell<Ad>() {
            @Override
            protected void updateItem(Ad ad, boolean empty) {
                super.updateItem(ad, empty);
                if (empty || ad == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Форматируем текст: "Название - Цена руб. (Город)"
                    setText(String.format("%s - %.2f руб. (%s)",
                            ad.getTitle(),
                            ad.getPrice(),
                            ad.getLocation()));
                }
            }
        });

        loadAds();
    }

    @Override
    public void onAuthStateChanged() {
        updateUIBasedOnAuthStatus();
    }

    @Override
    public void updateUIBasedOnAuthStatus() {
        boolean loggedIn = sessionManager.isLoggedIn();
        authBlock.setVisible(!loggedIn);
        authBlock.setManaged(!loggedIn);
        userBlock.setVisible(loggedIn);
        userBlock.setManaged(loggedIn);

        if (loggedIn) {
            usernameLabel.setText(sessionManager.getLoggedInUsername());
            adminButton.setVisible(sessionManager.isAdmin());
        } else {
            adminButton.setVisible(false);
        }
    }

    @Override
    public void refreshAds() {
        loadAds();
    }

    @Override
    @FXML
    public void openDatabaseView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/database_view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Просмотр базы данных");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть просмотр БД");
        }
    }

    @Override
    public void loadAds() {
        if (adsList == null) {
            System.err.println("adsList is not initialized!");
            return;
        }

        try {
            int userId = sessionManager.getLoggedInUserId();
            List<Ad> ads = inMemoryDatabase.loadAds();
            adsList.getItems().clear();
            if (ads == null || ads.isEmpty()) {
                System.out.println("Нет доступных объявлений для загрузки.");
                adsList.requestLayout();
                return;
            }

            adsList.getItems().clear();
            for (Ad ad : ads) {
                if (ad.getSellerId() != userId && "active".equals(ad.getStatus())) {
                    adsList.getItems().add(ad);
                    logAdDetails(ad);
                }
            }
            System.out.println("Всего загружено объявлений: " + adsList.getItems().size());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить объявления: " + e.getMessage());
        }
    }

    private void logAdDetails(Ad ad) {
        System.out.println("Загружено объявление: ID=" + ad.getAdId()
                + ", Title='" + ad.getTitle()
                + "', Price=" + ad.getPrice()
                + ", Status=" + ad.getStatus());
    }

    @Override
    @FXML
    public void openBasket() {
        try {
            sceneManager.showScene("basket");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть страницу корзины.");
            throw e;
        }
    }

    @Override
    @FXML
    public void openChatWindow() {
        try {
            sceneManager.showScene("chat");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть страницу чата.");
            throw e;
        }
    }

    @Override
    @FXML
    public void openProfile() {
        try {
            sceneManager.showScene("profile");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть страницу профиля.");
            throw e;
        }
    }

    @Override
    @FXML
    public void createAd() {
        try {
            sceneManager.showScene("create_ad");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть страницу создания объявления.");
            throw e;
        }
    }

    @Override
    @FXML
    public void handleSearch() {
        String query = searchField.getText();
        if (query.isEmpty()) {
            loadAds();
            return;
        }

        try {
            int userId = sessionManager.getLoggedInUserId();
            List<Ad> ads = inMemoryDatabase.searchAds(query, userId);
            adsList.getItems().clear();
            adsList.getItems().addAll(ads);
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось выполнить поиск: " + e.getMessage());
        }
    }

    @Override
    @FXML
    public void handleLogin() {
        try {
            sceneManager.showScene("login");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось загрузить страницу входа.");
            throw e;
        }
    }

    @Override
    @FXML
    public void handleRegister() {
        try {
            sceneManager.showScene("register");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить страницу регистрации. Причина: " + e.getMessage());
            throw e;
        }
    }

    @Override
    @FXML
    public void handleLogout() {
        try {
            sessionManager.logout();
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось выйти: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    @FXML
    public void openFiltersDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/filters.fxml"));
            Parent root = loader.load();
            FiltersController controller = loader.getController();
            controller.setOnApplyCallback(this::applyFilters);
            controller.setOnResetCallback(this::loadAds);
            Stage stage = new Stage();
            stage.setTitle("Фильтры объявлений");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть фильтры");
        }
    }

    @Override
    public void applyFilters(FiltersController.FilterParams params) {
        try {
            int userId = sessionManager.getLoggedInUserId();
            List<Ad> ads = inMemoryDatabase.getFilteredAds(
                    userId,
                    params.category,
                    params.city,
                    params.minPrice,
                    params.maxPrice
            );

            adsList.getItems().clear();
            if (ads != null && !ads.isEmpty()) {
                adsList.getItems().addAll(ads);
                System.out.println("Найдено объявлений после фильтрации: " + ads.size());
            } else {
                System.out.println("По заданным фильтрам объявления не найдены");
                showAlert("Информация", "По заданным фильтрам объявления не найдены");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось применить фильтры: " + e.getMessage());
        }
    }

    @Override
    @FXML
    public void handleAdDoubleClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Ad selectedAd = adsList.getSelectionModel().getSelectedItem();
            if (selectedAd != null) {
                try {
                    sceneManager.showSceneWithParameters("ad_details", "adId", selectedAd.getAdId());
                } catch (Exception e) {
                    showAlert("Ошибка", "Не удалось открыть описание объявления.");
                }
            } else {
                showAlert("Ошибка", "Выберите объявление для просмотра.");
            }
        }
    }
}