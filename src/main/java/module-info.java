module org.example.main {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    // Экспортируем пакеты, чтобы они были доступны другим модулям
    exports org.example.main; // Основной пакет
    exports org.example.main.controllers; // Контроллеры
    exports org.example.main.utils; // Утилиты
    exports org.example.main.models; // Модели данных

    // Открываем пакеты для рефлексии (необходимо для JavaFX и FXML)
    opens org.example.main.controllers to javafx.fxml;
    opens org.example.main.models to javafx.base, javafx.fxml; // Для работы с моделями в FXML
    opens org.example.main.utils to javafx.fxml; // Для работы с утилитами в FXML
    opens org.example.main to javafx.graphics, javafx.fxml; // Для запуска приложения и FXML
}