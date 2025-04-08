package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.main.models.Ad;
import org.example.main.utils.Database;
import org.example.main.utils.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BasketController {

    @FXML
    private ListView<Ad> basketList;

    @FXML
    private Label totalPriceLabel;

    /**
     * Инициализация контроллера.
     */
    @FXML
    public void initialize() {
        loadBasketItems();
    }
    @FXML
    private void handleBackToIndex() {
        try {
            // Загружаем index.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/index.fxml"));
            Parent root = loader.load();

            // Получаем текущую сцену
            Stage stage = (Stage) basketList.getScene().getWindow();

            // Устанавливаем новую сцену
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось перейти на главную страницу.");
        }
    }
    /**
     * Загрузка товаров в корзину.
     */
    private void loadBasketItems() {
        List<Ad> basketItems = getBasketItems();
        basketList.getItems().clear();
        basketList.getItems().addAll(basketItems);

        // Обновляем общую стоимость
        double totalPrice = basketItems.stream().mapToDouble(Ad::getPrice).sum();
        totalPriceLabel.setText(String.format("%.2f руб.", totalPrice));
    }

    /**
     * Получает товары из корзины текущего пользователя.
     */
    private List<Ad> getBasketItems() {
        List<Ad> basketItems = new ArrayList<>();
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT AD_ID, TITLE, PRICE, USER_ID FROM ADS WHERE AD_ID IN (SELECT AD_ID FROM USER_BASKET WHERE USER_ID = ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, SessionManager.getLoggedInUserId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Ad ad = new Ad();
                ad.setAdId(rs.getInt("AD_ID"));
                ad.setTitle(rs.getString("TITLE"));
                ad.setPrice(rs.getDouble("PRICE"));
                ad.setSellerId(rs.getInt("USER_ID"));
                basketItems.add(ad);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return basketItems;
    }

    /**
     * Очищает корзину.
     */
    @FXML
    private void handleClearBasket() {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM BASKET WHERE USER_ID = ?");
            stmt.setInt(1, SessionManager.getLoggedInUserId());
            stmt.executeUpdate();
            loadBasketItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Обрабатывает покупку товаров из корзины.
     */
    @FXML
    private void handleBuy() {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            for (Ad ad : basketList.getItems()) {
                if (ad.getSellerId() == SessionManager.getLoggedInUserId()) {
                    showAlert("Ошибка", "Вы не можете купить свой собственный товар: " + ad.getTitle());
                    continue;
                }

                updateAdStatus(conn, ad.getAdId());

                addPurchaseRecord(conn, ad);

                removeAdFromBasket(conn, ad.getAdId());
            }

            conn.commit();
            showAlert("Успех", "Покупка успешно завершена!");
            loadBasketItems();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Database.getConnection().rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            showAlert("Ошибка", "Не удалось завершить покупку.");
        }
    }

    /**
     * Обновляет статус объявления на 'sold'.
     */
    private void updateAdStatus(Connection conn, int adId) throws Exception {
        PreparedStatement stmt = conn.prepareStatement("UPDATE ADS SET STATUS = 'sold' WHERE AD_ID = ?");
        stmt.setInt(1, adId);
        stmt.executeUpdate();
    }

    /**
     * Добавляет запись о покупке.
     */
    private void addPurchaseRecord(Connection conn, Ad ad) throws Exception {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO PURCHASES (USER_ID, AD_ID, PRICE, PURCHASE_DATE) VALUES (?, ?, ?, CURRENT_TIMESTAMP)"
        );
        stmt.setInt(1, SessionManager.getLoggedInUserId());
        stmt.setInt(2, ad.getAdId());
        stmt.setDouble(3, ad.getPrice());
        stmt.executeUpdate();
    }

    /**
     * Удаляет товар из корзины.
     */
    private void removeAdFromBasket(Connection conn, int adId) throws Exception {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM BASKET WHERE AD_ID = ? AND USER_ID = ?");
        stmt.setInt(1, adId);
        stmt.setInt(2, SessionManager.getLoggedInUserId());
        stmt.executeUpdate();
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