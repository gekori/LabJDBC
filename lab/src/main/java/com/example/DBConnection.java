package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.TimeZone; // <--- 1. Додайте цей імпорт

public class DBConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    public static Connection getConnection() {
        // <--- 2. Додайте цей рядок, щоб уникнути помилки TimeZone
        TimeZone.setDefault(TimeZone.getTimeZone("UTC")); 

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("З'єднання з БД встановлено успiшно!");
        } catch (SQLException e) {
            System.err.println("Помилка з'єднання: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    public static void main(String[] args) {
        getConnection();
    }
}