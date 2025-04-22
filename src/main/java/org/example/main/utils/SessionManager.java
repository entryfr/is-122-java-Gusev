package org.example.main.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class SessionManager {


    private static final Logger logger = Logger.getLogger(SessionManager.class.getName());

    private static final ConcurrentHashMap<String, Object> sessionData = new ConcurrentHashMap<>();

    private static final CopyOnWriteArrayList<CartItem> basket = new CopyOnWriteArrayList<>();
    public static String getLoggedInUsername() {
        return (String) sessionData.get("username");
    }
    /**
     * Устанавливает данные авторизованного пользователя.
     *
     * @param username Имя пользователя
     * @param userId   ID пользователя
     */
    public static void setLoggedInUser(String username, int userId) {
        sessionData.put("username", username);
        sessionData.put("userId", userId);
        logger.info("Пользователь " + username + " успешно авторизован.");
    }

    /**
     * Проверяет, авторизован ли пользователь.
     *
     * @return true, если пользователь авторизован, иначе false
     */
    public static boolean isLoggedIn() {
        return sessionData.containsKey("username") && sessionData.containsKey("userId");
    }

    /**
     * Возвращает имя авторизованного пользователя.
     *
     * @return Имя пользователя или null, если пользователь не авторизован
     */
    public static String getLoggedInUser() {
        return (String) sessionData.get("username");
    }

    /**
     * Возвращает ID авторизованного пользователя.
     *
     * @return ID пользователя или -1, если пользователь не авторизован
     */
    public static int getLoggedInUserId() {
        return isLoggedIn() ? (int) sessionData.get("userId") : -1;
    }

    /**
     * Выполняет выход пользователя.
     */
    public static void logout() {
        sessionData.clear();
        basket.clear();
        logger.info("Пользователь вышел из системы.");
    }

    /**
     * Добавляет товар в корзину.
     *
     * @param title Название товара
     * @param price Цена товара
     */
    public static void addToBasket(String title, double price) {
        CartItem item = new CartItem(title, price);
        basket.add(item);
        logger.info("Товар добавлен в корзину: " + item);
    }

    /**
     * Возвращает список всех товаров в корзине.
     *
     * @return Список товаров в корзине
     */
    public static List<CartItem> getBasketItems() {
        return basket;
    }

    /**
     * Удаляет товар из корзины по названию.
     *
     * @param title Название товара
     */
    public static void removeFromBasket(String title) {
        basket.removeIf(item -> title.equals(item.getTitle()));
        logger.info("Товар удален из корзины: " + title);
    }

    /**
     * Очищает корзину.
     */
    public static void clearBasket() {
        basket.clear();
        logger.info("Корзина очищена.");
    }

    /**
     * Возвращает общую стоимость товаров в корзине.
     *
     * @return Общая стоимость
     */
    public static double getTotalPrice() {
        return basket.stream()
                .mapToDouble(CartItem::getPrice)
                .sum();
    }

    /**
     * Возвращает количество товаров в корзине.
     *
     * @return Количество товаров
     */
    public static int getBasketItemCount() {
        return basket.size();
    }

    /**
     * Класс для представления товара в корзине.
     */
    public static class CartItem {
        private final String title;
        private final double price;

        public CartItem(String title, double price) {
            this.title = title;
            this.price = price;
        }

        public String getTitle() {
            return title;
        }

        public double getPrice() {
            return price;
        }

        @Override
        public String toString() {
            return "CartItem{" +
                    "title='" + title + '\'' +
                    ", price=" + price +
                    '}';
        }
    }
}