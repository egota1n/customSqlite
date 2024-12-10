package org.example.CustomSqlite;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ReadCustomSqlite {

    private static final String SQLITE_SIGNATURE = "SQLite format 3\0";

    public static void main(String[] args) throws IOException {
        byte[] fileBytes = readFile(new File("data.db"));

        // Проверка заголовка
        if (!SQLITE_SIGNATURE.equals(new String(fileBytes, 0, 16, StandardCharsets.UTF_8))) {
            throw new IOException("Неверный формат файла");
        }

        int pageSize = readPageSize(fileBytes);
        PageParserSqlite parser = new PageParserSqlite(pageSize);

        // Читаем первую страницу (sqlite_master)
        byte[] page1 = parser.readPage(fileBytes, 0);
        List<Integer> masterCellOffsets = parser.getCellOffsets(page1, 100);

        // Поиск rootpage таблицы users
        int usersRootPage = -1;
        for (int off : masterCellOffsets) {
            PageParserSqlite.Record rec = parser.parseTableLeafCell(page1, off, true);
            if (rec != null && rec.values.size() >= 4 && "users".equals(rec.values.get(1))) {
                usersRootPage = ((Number) rec.values.get(3)).intValue();
                break;
            }
        }
        if (usersRootPage == -1) throw new IOException("Таблица 'users' не найдена.");

        // Читаем страницу таблицы users
        byte[] usersPage = parser.readPage(fileBytes, usersRootPage - 1);
        List<Integer> usersOffsets = parser.getCellOffsets(usersPage, (usersRootPage - 1 == 0) ? 100 : 0);

        // Вывод данных из таблицы users
        for (int off : usersOffsets) {
            PageParserSqlite.Record r = parser.parseTableLeafCell(usersPage, off, false);
            if (r != null && r.values.size() == 2) {
                System.out.println("Name: " + r.values.get(0) + ", Age: " + r.values.get(1));
            }
        }
    }

    private static byte[] readFile(File f) throws IOException {
        byte[] d = new byte[(int) f.length()];
        try (FileInputStream fis = new FileInputStream(f)) {
            if (fis.read(d) != d.length) throw new IOException("Не удалось считать файл полностью");
        }
        return d;
    }

    private static int readPageSize(byte[] fileBytes) {
        int ps = ((fileBytes[16] & 0xFF) << 8) | (fileBytes[17] & 0xFF);
        return ps == 1 ? 65536 : ps;
    }
}