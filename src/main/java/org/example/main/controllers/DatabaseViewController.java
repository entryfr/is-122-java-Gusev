package org.example.main.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.sql.*;

public class DatabaseViewController {
    @FXML
    private ComboBox<String> tablesCombo;

    @FXML
    private TableView<ObservableList<String>> dataTable;

    @FXML
    public void initialize() {
        loadTables();
        tablesCombo.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> loadTableData(newVal));
    }

    private void loadTables() {
        try {
            DatabaseMetaData meta = InMemoryDatabase.getInstance().getConnection().getMetaData();
            ResultSet tables = meta.getTables(null, null, null, new String[]{"TABLE"});

            while (tables.next()) {
                tablesCombo.getItems().add(tables.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshData() {
        String selectedTable = tablesCombo.getValue();
        if (selectedTable != null) {
            loadTableData(selectedTable);
        }
    }

    private void loadTableData(String tableName) {
        try {
            Statement stmt = InMemoryDatabase.getInstance().getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData metaData = rs.getMetaData();

            dataTable.getColumns().clear();
            dataTable.getItems().clear();

            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                final int columnIndex = i; // final переменная для использования в лямбде
                TableColumn<ObservableList<String>, String> column = new TableColumn<>(metaData.getColumnName(i));
                column.setCellValueFactory(param -> {
                    ObservableList<String> row = param.getValue();
                    String value = row.get(columnIndex - 1);
                    return new SimpleStringProperty(value != null ? value : "");
                });
                dataTable.getColumns().add(column);
            }

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getString(i));
                }
                dataTable.getItems().add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}