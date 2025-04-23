package org.example.main.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.main.controllers.ChatController;
import org.example.main.controllers.InMemoryDatabase;
import org.example.main.models.Ad;

public class AdCellFactory implements Callback<ListView<Ad>, ListCell<Ad>> {

    private final InMemoryDatabase inMemoryDatabase = InMemoryDatabase.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @Override
    public ListCell<Ad> call(ListView<Ad> listView) {
        return new ListCell<>() {
            @Override
            protected void updateItem(Ad item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(createItemCell(item));
                }
            }
        };
    }

    private HBox createItemCell(Ad item) {
        Text titleText = new Text(item.getTitle());
        Text priceText = new Text(String.format("%.2f руб.", item.getPrice()));
        Text locationText = new Text(item.getLocation());

        Button buyButton = createBuyButton(item);
        Button messageButton = createMessageButton(item);

        if (item.getSellerId() == sessionManager.getLoggedInUserId()) {
            titleText.setFill(Color.RED);
        } else {
            titleText.setFill(Color.BLACK);
        }

        HBox hbox = new HBox(10, titleText, priceText, locationText, buyButton, messageButton);
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return hbox;
    }

    private Button createBuyButton(Ad item) {
        Button buyButton = new Button("Купить");
        buyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        if (item.getSellerId() == sessionManager.getLoggedInUserId()) {
            buyButton.setDisable(true);
            buyButton.setText("Ваш товар");
            buyButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");
        } else {
            buyButton.setOnAction(event -> handleBuy(item));
        }
        return buyButton;
    }

    private Button createMessageButton(Ad item) {
        Button messageButton = new Button("Написать продавцу");
        messageButton.setOnAction(event -> openChatWithSeller(item.getSellerId()));
        return messageButton;
    }

    private void handleBuy(Ad ad) {
        try {
            int userId = sessionManager.getLoggedInUserId();

            if (userId == -1) {
                showAlert("Ошибка", "Необходимо авторизоваться для покупки");
                return;
            }

            if (ad.getSellerId() == userId) {
                showAlert("Ошибка", "Вы не можете купить свой собственный товар: " + ad.getTitle());
                return;
            }

            // Проверяем существование объявления перед добавлением в корзину
            if (!inMemoryDatabase.adExists(ad.getAdId())) {
                showAlert("Ошибка", "Объявление с ID " + ad.getAdId() + " не найдено");
                return;
            }

            inMemoryDatabase.addToBasket(userId, ad.getAdId());
            showAlert("Успех", "Товар успешно добавлен в корзину!");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось добавить товар в корзину: " + e.getMessage());
        }
    }

    private void openChatWithSeller(int sellerId) {
        try {
            int currentUserId = sessionManager.getLoggedInUserId();
            if (currentUserId == -1) {
                showAlert("Ошибка", "Необходимо авторизоваться для отправки сообщений");
                return;
            }

            int chatId = inMemoryDatabase.getOrCreateChat(currentUserId, sellerId);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/chat.fxml"));
            Parent root = loader.load();

            ChatController chatController = loader.getController();
            chatController.setChatId(chatId);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Чат с продавцом");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть чат: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}