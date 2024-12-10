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
                DROP TABLE IF EXISTS users;
                CREATE TABLE users (
                    name TEXT,
                    age INTEGER
                );
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS users;");
            stmt.execute("CREATE TABLE users (name TEXT, age INTEGER);");
            System.out.println("Таблица users успешно создана");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertData() {
        String sql = """
                INSERT INTO users (name, age) VALUES
                ('Egor', 22),
                ('Artem', 22),
                ('Timur', 22),
                ('Gena', 40),
                ('Max', 45);
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            int rowsInserted = stmt.executeUpdate(sql);
            System.out.println(rowsInserted + " строк добавлено в таблицу users");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}