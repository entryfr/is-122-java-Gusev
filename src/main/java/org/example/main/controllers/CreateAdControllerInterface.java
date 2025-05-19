package org.example.main.controllers;
import javafx.fxml.FXML;
import java.sql.SQLException;
public interface CreateAdControllerInterface {
    @FXML
    void initialize();

    void loadCategories() throws SQLException;
    void initializeCitiesComboBox() throws SQLException;
    void handleImageUpload();
    void handleCreateAd() throws Exception;
    void showAlert(String title, String message);

    void refreshAdsOnIndexPage() throws Exception;
    void redirectToLogin() throws Exception;

}