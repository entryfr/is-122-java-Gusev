package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.main.models.Ad;
import org.example.main.utils.SceneManager;
import org.example.main.utils.SessionManager;

import java.io.ByteArrayInputStream;

public class AdDetailsController {

    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private TextArea descriptionField;
    @FXML private Label locationLabel;
    @FXML private ImageView imageView;
    @FXML private Button buyButton;
    @FXML private Button messageButton;

    private int adId;

    @FXML
    public void initialize() {
        if (buyButton == null || messageButton == null) {
            System.err.println("Ошибка: Не все FXML-элементы были загружены!");
            return;
        }

        adId = (int) SceneManager.getInstance().getPassedParameter("adId", -1);
        if (adId > 0) {
            loadAdDetails(adId);
        } else {
            System.err.println("Ошибка: ID объявления не передан.");
        }
    }

    private void loadAdDetails(int adId) {
        try {
            InMemoryDatabase db = InMemoryDatabase.getInstance();
            Ad ad = getAdById(db, adId);

            if (ad != null) {
                titleLabel.setText(ad.getTitle());
                priceLabel.setText(String.format("%.2f руб.", ad.getPrice()));
                descriptionField.setText(ad.getDescription());
                locationLabel.setText(ad.getLocation());

                if (ad.getImage() != null) {
                    Image image = new Image(new ByteArrayInputStream(ad.getImage()));
                    imageView.setImage(image);
                }

                int loggedInUserId = SessionManager.getInstance().getLoggedInUserId();
                if (loggedInUserId == ad.getSellerId()) {
                    buyButton.setDisable(true);
                    buyButton.setText("Это ваше объявление");
                } else {
                    buyButton.setOnAction(event -> handleBuy(ad));
                }

                messageButton.setOnAction(event -> openChatWithSeller(ad.getSellerId()));
            } else {
                System.err.println("Объявление с ID " + adId + " не найдено.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить данные объявления.");
        }
    }

    @FXML
    private void goBack() {
        try {
            SceneManager.getInstance().showScene("index");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось вернуться на предыдущую страницу.");
        }
    }

    /**
     * Получение объявления по его ID.
     */
    private Ad getAdById(InMemoryDatabase db, int adId) throws Exception {
        String query = """
            SELECT AD_ID, TITLE, PRICE, DESCRIPTION, IMAGE_PATH, LOCATION, STATUS, USER_ID
            FROM ADS
            WHERE AD_ID = ? AND STATUS = 'active'
        """;
        try (var stmt = db.getConnection().prepareStatement(query)) {
            stmt.setInt(1, adId);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                Ad ad = new Ad();
                ad.setAdId(rs.getInt("AD_ID"));
                ad.setTitle(rs.getString("TITLE"));
                ad.setPrice(rs.getDouble("PRICE"));
                ad.setDescription(rs.getString("DESCRIPTION"));
                ad.setImage(rs.getBytes("IMAGE_PATH"));
                ad.setLocation(rs.getString("LOCATION"));
                ad.setStatus(rs.getString("STATUS"));
                ad.setSellerId(rs.getInt("USER_ID"));
                return ad;
            }
        }
        return null;
    }

    /**
     * Обработка покупки товара.
     */
    private void handleBuy(Ad ad) {
        try {
            InMemoryDatabase db = InMemoryDatabase.getInstance();
            int userId = SessionManager.getInstance().getLoggedInUserId();

            if (ad.getSellerId() == userId) {
                showAlert("Ошибка", "Вы не можете купить свой собственный товар.");
                return;
            }

            db.addToBasket(userId, ad.getAdId());
            showAlert("Успех", "Товар успешно добавлен в корзину!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось добавить товар в корзину.");
        }
    }

    /**
     * Открытие чата с продавцом.
     */
    private void openChatWithSeller(int sellerId) {
        try {
            InMemoryDatabase db = InMemoryDatabase.getInstance();
            int currentUserId = SessionManager.getInstance().getLoggedInUserId();

            int chatId = db.getOrCreateChat(currentUserId, sellerId);

            SceneManager.getInstance().showSceneWithParameters("chat", "chatId", chatId);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть чат.");
        }
    }

    /**
     * Отображение диалогового окна с сообщением.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}