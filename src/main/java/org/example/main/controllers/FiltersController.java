package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.function.Consumer;

public class FiltersController {
    @FXML private ComboBox<String> categoryFilter;
    @FXML private TextField minPriceFilter;
    @FXML private TextField maxPriceFilter;
    @FXML private ComboBox<String> cityFilter; // Добавлен ComboBox для городов

    private Consumer<FilterParams> onApplyCallback;
    private Runnable onResetCallback;

    public void initialize() {
        try {
            // Инициализация фильтра категорий
            categoryFilter.getItems().addAll(InMemoryDatabase.getInstance().loadCategories());
            categoryFilter.getItems().add(0, "Все категории");
            categoryFilter.getSelectionModel().selectFirst();

            // Инициализация фильтра городов
            cityFilter.getItems().addAll(InMemoryDatabase.getInstance().getCities());
            cityFilter.getItems().add(0, "Все города");
            cityFilter.getSelectionModel().selectFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void applyFilters() {
        if (onApplyCallback != null) {
            try {
                String category = categoryFilter.getSelectionModel().getSelectedItem();
                if ("Все категории".equals(category) || category == null) {
                    category = null;
                }

                String city = cityFilter.getSelectionModel().getSelectedItem();
                if ("Все города".equals(city) || city == null) {
                    city = null;
                }

                Double minPrice = parsePrice(minPriceFilter.getText());
                Double maxPrice = parsePrice(maxPriceFilter.getText());

                if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText(null);
                    alert.setContentText("Минимальная цена не может быть больше максимальной");
                    alert.showAndWait();
                    return;
                }

                System.out.println("Применяем фильтры: category=" + category +
                        ", city=" + city +
                        ", minPrice=" + minPrice + ", maxPrice=" + maxPrice);

                onApplyCallback.accept(new FilterParams(category, city, minPrice, maxPrice));
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText("Ошибка при применении фильтров: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void resetFilters() {
        categoryFilter.getSelectionModel().selectFirst();
        cityFilter.getSelectionModel().selectFirst();
        minPriceFilter.clear();
        maxPriceFilter.clear();
        if (onResetCallback != null) {
            onResetCallback.run();
        }
    }

    private Double parsePrice(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void setOnApplyCallback(Consumer<FilterParams> callback) {
        this.onApplyCallback = callback;
    }

    public void setOnResetCallback(Runnable callback) {
        this.onResetCallback = callback;
    }

    public static class FilterParams {
        public final String category;
        public final String city; // Добавлено поле для города
        public final Double minPrice;
        public final Double maxPrice;

        public FilterParams(String category, String city, Double minPrice, Double maxPrice) {
            this.category = category;
            this.city = city;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
    }
}