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
import org.example.main.controllers.IndexController;
import org.example.main.models.Ad;
import org.example.main.utils.Database;
import org.example.main.utils.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdCellFactory implements Callback<ListView<Ad>, ListCell<Ad>> {

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
                        IndexController.handleBuy(item.getAdId());
                    });


                    Button messageButton = new Button("Написать продавцу");
                    messageButton.setOnAction(event -> openChatWithSeller(item.getSellerId()));

                    if (item.getSellerId() == SessionManager.getLoggedInUserId()) {
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
     * Открытие чата с продавцом.
     */
    private void openChatWithSeller(int sellerId) {
        try {
            int currentUserId = SessionManager.getLoggedInUserId();
            int chatId = getOrCreateChat(currentUserId, sellerId);

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

    /**
     * Получение или создание чата между текущим пользователем и продавцом.
     */
    private int getOrCreateChat(int user1Id, int user2Id) throws Exception {
        try (Connection conn = Database.getConnection()) {
            String checkQuery = "SELECT CHAT_ID FROM CHATS WHERE (USER1_ID = ? AND USER2_ID = ?) OR (USER1_ID = ? AND USER2_ID = ?)";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, user1Id);
            checkStmt.setInt(2, user2Id);
            checkStmt.setInt(3, user2Id);
            checkStmt.setInt(4, user1Id);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("CHAT_ID");
            } else {
                String insertQuery = "INSERT INTO CHATS (CHAT_ID, USER1_ID, USER2_ID, LAST_MESSAGE, LAST_MESSAGE_TIME) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                int chatId = generateChatId();
                insertStmt.setInt(1, chatId);
                insertStmt.setInt(2, user1Id);
                insertStmt.setInt(3, user2Id);
                insertStmt.setString(4, "Новый чат");
                insertStmt.executeUpdate();
                return chatId;
            }
        }
    }

    /**
     * Генерация уникального ID чата.
     */
    private int generateChatId() {
        return (int) (Math.random() * 1_000_000);
    }
}