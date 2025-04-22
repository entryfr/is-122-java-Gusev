package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.main.models.Ad;
import org.example.main.utils.Database;
import org.example.main.utils.SceneManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdDetailsController {

    @FXML
    private ImageView imageView;

    @FXML
    private Label titleLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private TextArea descriptionField;

    @FXML
    private Label locationLabel;

    private int adId;

    /**
     * Инициализация контроллера.
     */
    @FXML
    public void initialize() {
        adId = SceneManager.getPassedParameter("adId", -1); // Получаем ID объявления из параметров
        if (adId != -1) {
            loadAdDetails(adId); // Загружаем данные объявления
        } else {
            System.out.println("ID объявления не передан.");
        }
    }

    /**
     * Загрузка данных объявления.
     */
    private void loadAdDetails(int adId) {
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT TITLE, PRICE, DESCRIPTION, IMAGE_PATH, LOCATION FROM ADS WHERE AD_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, adId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Установка названия
                titleLabel.setText(rs.getString("TITLE"));

                // Установка цены
                priceLabel.setText(String.valueOf(rs.getDouble("PRICE")));

                // Установка описания
                descriptionField.setText(rs.getString("DESCRIPTION"));

                // Установка местоположения
                locationLabel.setText(rs.getString("LOCATION"));

                // Установка изображения
                byte[] imageBytes = rs.getBytes("IMAGE_PATH");
                if (imageBytes != null) {
                    Image image = new Image(new java.io.ByteArrayInputStream(imageBytes));
                    imageView.setImage(image);
                } else {
                    imageView.setImage(null); // Если изображение отсутствует
                }
            } else {
                System.out.println("Объявление с ID " + adId + " не найдено.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Не удалось загрузить данные объявления.");
        }
    }

    /**
     * Возврат на предыдущую страницу.
     */
    @FXML
    private void goBack() {
        try {
            SceneManager.showScene("index");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Не удалось вернуться на главную страницу.");
        }
    }
}