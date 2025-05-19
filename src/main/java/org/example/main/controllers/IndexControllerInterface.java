package org.example.main.controllers;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
public interface IndexControllerInterface {
    @FXML
    void initialize();
    void loadAds();
    void refreshAds();
    void updateUIBasedOnAuthStatus();
    @FXML
    void handleSearch();
    void applyFilters(FiltersController.FilterParams params);
    @FXML
    void openBasket() throws Exception;
    @FXML
    void openChatWindow() throws Exception;
    @FXML
    void openProfile() throws Exception;
    @FXML
    void createAd() throws Exception;
    @FXML
    void handleLogin() throws Exception;
    @FXML
    void handleRegister() throws Exception;
    @FXML
    void handleLogout() throws Exception;
    @FXML
    void openFiltersDialog();
    @FXML
    void handleAdDoubleClick(MouseEvent event);
    @FXML
    void openDatabaseView();
    void showAlert(String title, String message);
}