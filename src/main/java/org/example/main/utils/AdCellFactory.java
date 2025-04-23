package org.example.main.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    private final InMemoryDatabase inMemoryDatabase = new InMemoryDatabase();
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
                    Text titleText = new Text(item.getTitle());
                    Text priceText = new Text(String.format("%.2f руб.", item.getPrice()));
                    Text locationText = new Text(item.getLocation());

                    Button buyButton = new Button("Купить");
                    buyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    buyButton.setOnAction(event -> {
                        System.out.println("Покупка товара: " + item.getTitle());
                        handleBuy(item.getAdId());
                    });

                    Button messageButton = new Button("Написать продавцу");
                    messageButton.setOnAction(event -> openChatWithSeller(item.getSellerId()));

                    if (item.getSellerId() == sessionManager.getLoggedInUserId()) {
                        titleText.setFill(Color.RED);
                    } else {
                        titleText.setFill(Color.BLACK);
                    }

                    HBox hbox = new HBox(10, titleText, priceText, locationText, buyButton, messageButton);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    setGraphic(hbox);
                }
            }
        };
    }

    /**
     * Обработка покупки товара.
     */
    private void handleBuy(int adId) {
        try {
            int userId = SessionManager.getInstance().getLoggedInUserId();
            if (userId == -1) {
                System.out.println("Необходимо авторизоваться");
                return;
            }

            double price = InMemoryDatabase.getInstance().getPriceForAd(adId);
            InMemoryDatabase.getInstance().addToBasket(userId, adId);
            System.out.println("Товар добавлен в корзину");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Открытие чата с продавцом.
     */
    private void openChatWithSeller(int sellerId) {
        try {
            int currentUserId = sessionManager.getLoggedInUserId();
            int chatId = inMemoryDatabase.getOrCreateChat(currentUserId, sellerId);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main/chat.fxml"));
            Parent root = loader.load();

            ChatController chatController = loader.getController();
            chatController.setChatId(chatId);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Чат");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Не удалось открыть чат.");
        }
    }
}