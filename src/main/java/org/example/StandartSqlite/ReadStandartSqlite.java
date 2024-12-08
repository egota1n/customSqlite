package org.example.StandartSqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ReadStandartSqlite {

    private static final String DB_URL = "jdbc:sqlite:data.db";

    public static void main(String[] args) {
        readData();
    }

    private static void readData() {
        String sql = "SELECT * FROM users";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Чтение данных из таблицы пользователей");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int age = rs.getInt("age");

                System.out.printf("ID: %d, Name: %s, Age: %d%n", id, name, age);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}