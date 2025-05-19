package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.main.models.Ad;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;

import java.util.List;

public class BasketController {

    @FXML
    private ListView<Ad> basketList;

    @FXML
    private Label totalPriceLabel;

    private final InMemoryDatabase inMemoryDatabase = InMemoryDatabase.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void initialize() {
        basketList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Ad ad, boolean empty) {
                super.updateItem(ad, empty);
                if (empty || ad == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - %.2f руб.", ad.getTitle(), ad.getPrice()));
                }
            }
        });

        loadBasketItems();
    }

    private void loadBasketItems() {
        try {
            List<Ad> basketItems = inMemoryDatabase.getBasketItems(sessionManager.getLoggedInUserId());

            basketList.getItems().setAll(basketItems);

            updateTotalPrice(basketItems);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить товары из корзины.");
        }
    }

    private void updateTotalPrice(List<Ad> items) {
        double totalPrice = items.stream()
                .mapToDouble(Ad::getPrice)
                .sum();
        totalPriceLabel.setText(String.format("%.2f руб.", totalPrice));
    }

    @FXML
    private void handleClearBasket() {
        try {
            inMemoryDatabase.clearBasket(sessionManager.getLoggedInUserId());
            basketList.getItems().clear();
            totalPriceLabel.setText("0.00 руб.");
            showAlert("Успех", "Корзина очищена.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось очистить корзину.");
        }
    }

    @FXML
    private void handleBuy() {
        try {
            List<Ad> items = basketList.getItems();
            if (items.isEmpty()) {
                showAlert("Ошибка", "Корзина пуста.");
                return;
            }

            for (Ad ad : items) {
                if (ad.getSellerId() == sessionManager.getLoggedInUserId()) {
                    showAlert("Ошибка", "Вы не можете купить свой собственный товар: " + ad.getTitle());
                    continue;
                }

                inMemoryDatabase.updateAdStatus(ad.getAdId());
                int purchaseId = generateUniquePurchaseId(ad.getAdId(), sessionManager.getLoggedInUserId());
                inMemoryDatabase.addPurchaseRecord(purchaseId, sessionManager.getLoggedInUserId(), ad);
                inMemoryDatabase.removeFromBasket(sessionManager.getLoggedInUserId(), ad.getAdId());
            }

            showAlert("Успех", "Покупка успешно завершена!");
            loadBasketItems();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось завершить покупку.");
        }
    }

    @FXML
    private void handleBackToIndex() {
        try {
            SceneManager.getInstance().showScene("index");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось вернуться на главную страницу.");
        }
    }

    private int generateUniquePurchaseId(int adId, int userId) {
        long timestamp = System.currentTimeMillis();
        return Math.abs((adId + userId + (int) (timestamp % 1000000)));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}