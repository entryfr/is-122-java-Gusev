package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.example.main.utils.SceneManager;

import java.util.function.Consumer;

public class FiltersController {
    @FXML private ComboBox<String> categoryFilter;
    @FXML private TextField minPriceFilter;
    @FXML private TextField maxPriceFilter;

    private Consumer<FilterParams> onApplyCallback;
    private Runnable onResetCallback;

    public void initialize() {
        try {
            categoryFilter.getItems().addAll(InMemoryDatabase.getInstance().loadCategories());
            categoryFilter.getItems().add(0, "Все категории");
            categoryFilter.getSelectionModel().selectFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void applyFilters() {
        if (onApplyCallback != null) {
            String category = categoryFilter.getSelectionModel().getSelectedItem();
            if ("Все категории".equals(category)) {
                category = null;
            }

            Double minPrice = parsePrice(minPriceFilter.getText());
            Double maxPrice = parsePrice(maxPriceFilter.getText());

            onApplyCallback.accept(new FilterParams(category, minPrice, maxPrice));
        }
    }

    @FXML
    private void resetFilters() {
        categoryFilter.getSelectionModel().selectFirst();
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
        public final Double minPrice;
        public final Double maxPrice;

        public FilterParams(String category, Double minPrice, Double maxPrice) {
            this.category = category;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
    }
}