package org.example.main.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

    private static final Logger LOGGER = Logger.getLogger(Database.class.getName());
    private static final String URL = "jdbc:firebirdsql://localhost/C:\\Users\\user152\\Desktop\\kursa4\\TESTKURS.fdb?encoding=UTF8";
    private static final String USER = "sysdba";
    private static final String PASSWORD = "123";

    /**
     * Получение соединения с базой данных.
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.firebirdsql.jdbc.FBDriver");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "JDBC драйвер Firebird не найден!", e);
            throw new RuntimeException("Ошибка загрузки JDBC драйвера Firebird", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Закрытие соединения с базой данных.
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Ошибка при закрытии соединения с базой данных.", e);
            }
        }
    }

    /**
     * Выполнение SQL-запроса SELECT с автоматическим закрытием ресурсов.
     *
     * @param query  SQL-запрос
     * @param params Параметры запроса
     * @return ResultSet с результатами запроса
     * @throws SQLException если произошла ошибка при выполнении запроса
     */
    public static ResultSet executeQuery(String query, Object... params) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(query);

            // Установка параметров в запросе
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            resultSet = statement.executeQuery();

            return resultSet;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при выполнении SQL-запроса: " + query, e);
            throw e;
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Ошибка при закрытии ResultSet", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Ошибка при закрытии PreparedStatement", e);
                }
            }
            closeConnection(connection);
        }
    }

    /**
     * Выполнение SQL-обновления (INSERT, UPDATE, DELETE) с автоматическим закрытием ресурсов.
     *
     * @param query  SQL-запрос
     * @param params Параметры запроса
     * @return Количество измененных строк
     * @throws SQLException если произошла ошибка при выполнении запроса
     */
    public static int executeUpdate(String query, Object... params) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(query);

            // Установка параметров в запросе
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            return statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при выполнении SQL-обновления: " + query, e);
            throw e;
        } finally {
            // Автоматическое закрытие ресурсов
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Ошибка при закрытии PreparedStatement", e);
                }
            }
            closeConnection(connection);
        }
    }

    /**
     * Выполнение SQL-запроса с обработкой результата через callback.
     *
     * @param query   SQL-запрос
     * @param handler Обработчик результата
     * @param params  Параметры запроса
     * @throws SQLException если произошла ошибка при выполнении запроса
     */
    public static void executeQueryWithHandler(String query, ResultSetHandler handler, Object... params) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(query);

            // Установка параметров в запросе
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            resultSet = statement.executeQuery();

            // Передаем результат в обработчик
            while (resultSet.next()) {
                handler.handle(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при выполнении SQL-запроса: " + query, e);
            throw e;
        } finally {
            // Автоматическое закрытие ресурсов
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Ошибка при закрытии ResultSet", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Ошибка при закрытии PreparedStatement", e);
                }
            }
            closeConnection(connection);
        }
    }

    /**
     * Интерфейс для обработки ResultSet.
     */
    @FunctionalInterface
    public interface ResultSetHandler {
        void handle(ResultSet resultSet) throws SQLException;
    }
}