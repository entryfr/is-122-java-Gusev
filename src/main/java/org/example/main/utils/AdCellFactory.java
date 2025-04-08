package org.example.main.utils;

import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.example.main.models.Ad;
import org.example.main.controllers.IndexController;

public class AdCellFactory implements Callback<ListView<Ad>, ListCell<Ad>> {

    // Конструктор по умолчанию
    public AdCellFactory() {
    }

    @Override
    public ListCell<Ad> call(ListView<Ad> listView) {
        return new ListCell<>() {
            @Override
            protected void updateItem(Ad item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Text titleText = new Text(item.getTitle() + " - " + item.getPrice() + " руб.");

                    Button buyButton = new Button("Купить");
                    buyButton.setOnAction(event -> {
                        System.out.println("Покупка товара: " + item.getTitle());
                        IndexController.handleBuy(item.getAdId());
                    });

                    HBox hbox = new HBox(10, titleText, buyButton);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    setGraphic(hbox);
                }
            }
        };
    }
}