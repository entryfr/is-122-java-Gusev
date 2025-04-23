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

    // Экземпляры зависимостей
    private final InMemoryDatabase inMemoryDatabase = new InMemoryDatabase();
    private final SessionManager sessionManager = SessionManager.getInstance();

    /**
     * Инициализация контроллера.
     */
    @FXML
    public void initialize() {
        loadBasketItems();
    }

    /**
     * Загрузка товаров в корзину.
     */
    private void loadBasketItems() {
        try {
            List<Ad> basketItems = inMemoryDatabase.getBasketItems(sessionManager.getLoggedInUserId());
            basketList.getItems().clear();
            basketList.getItems().addAll(basketItems);

            double totalPrice = basketItems.stream().mapToDouble(Ad::getPrice).sum();
            totalPriceLabel.setText(String.format("%.2f руб.", totalPrice));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить товары из корзины.");
        }
    }

    /**
     * Очищает корзину.
     */
    @FXML
    private void handleClearBasket() {
        try {
            inMemoryDatabase.clearBasket(sessionManager.getLoggedInUserId());
            loadBasketItems();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось очистить корзину.");
        }
    }

    /**
     * Обрабатывает покупку товаров из корзины.
     */
    @FXML
    private void handleBuy() {
        try {
            for (Ad ad : basketList.getItems()) {
                if (ad.getSellerId() == sessionManager.getLoggedInUserId()) {
                    showAlert("Ошибка", "Вы не можете купить свой собственный товар: " + ad.getTitle());
                    continue;
                }

                // Обновляем статус объявления
                inMemoryDatabase.updateAdStatus(ad.getAdId());

                // Добавляем запись о покупке
                int purchaseId = generateUniquePurchaseId(ad.getAdId(), sessionManager.getLoggedInUserId());
                inMemoryDatabase.addPurchaseRecord(purchaseId, sessionManager.getLoggedInUserId(), ad);

                // Удаляем товар из корзины
                inMemoryDatabase.removeFromBasket(sessionManager.getLoggedInUserId(), ad.getAdId());
            }

            showAlert("Успех", "Покупка успешно завершена!");
            loadBasketItems();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось завершить покупку.");
        }
    }

    /**
     * Генерирует уникальный числовой ID для покупки.
     */
    private int generateUniquePurchaseId(int adId, int userId) {
        long timestamp = System.currentTimeMillis();
        return Math.abs((adId + userId + (int) (timestamp % 1000000)));
    }

    /**
     * Отображает диалоговое окно с сообщением.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}