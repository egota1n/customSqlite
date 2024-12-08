package org.example.StandartSqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class WriteStandartSqlite {

    private static final String DB_URL = "jdbc:sqlite:data.db";

    public static void main(String[] args) {
        createDatabase();
        createTable();
        insertData();
    }

    private static void createDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                System.out.println("База данных успешно создана!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    age INTEGER
                );
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица пользователей успешно создана");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertData() {
        String sql = """
                INSERT INTO users (name, age) VALUES
                ('Egor', 22),
                ('Artem', 22),
                ('Max', 45);
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            int rowsInserted = stmt.executeUpdate(sql);
            System.out.println(rowsInserted + " строк добавлено в таблицу пользователей");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}