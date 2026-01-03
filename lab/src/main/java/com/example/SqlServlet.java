package com.example;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/execute")
public class SqlServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        
        String input = req.getParameter("sqlQuery");
        if (input == null) input = "";
        input = input.trim();

        String message = "";
        boolean isError = false;
        List<String> columnNames = new ArrayList<>();
        List<List<String>> dataList = new ArrayList<>();

        try {
            // --- ЛОГІКА ЗАХИЩЕНОГО ДОДАТКУ ---
            
            // 1. Якщо це команда SELECT (залишаємо для перевірки даних, але бажано теж захищати)
            if (input.toLowerCase().startsWith("select")) {
                performRead(input, columnNames, dataList);
                message = "Дані отримано. Кількість рядків: " + dataList.size();
            
            // 2. Якщо це команда INSERT (формат: insert table(key='val', ...))
            } else if (input.toLowerCase().startsWith("insert")) {
                message = handleSecureInsert(input);
            
            // 3. Якщо це команда DELETE (формат: delete table(id='val'))
            } else if (input.toLowerCase().startsWith("delete")) {
                message = handleSecureDelete(input);
            
            } else {
                throw new Exception("Невідома команда. Використовуйте формат:\ninsert department(name='...', head_of_department='...')\nабо delete department(id='...')");
            }

        } catch (Exception e) {
            isError = true;
            message = "Помилка: " + e.getMessage();
            e.printStackTrace();
        }

        req.setAttribute("resultMessage", message);
        req.setAttribute("isError", isError);
        req.setAttribute("columnNames", columnNames);
        req.setAttribute("dataList", dataList);
        
        req.getRequestDispatcher("index.jsp").forward(req, resp);
    }

    // --- МЕТОДИ ПАРСИНГУ ТА ЗАХИСТУ ---

    private String handleSecureInsert(String input) throws Exception {
        // Парсинг: insert department(name='A', head_of_department='B')
        String tableName = extractTableName(input);
        Map<String, String> params = extractParams(input);

        // Для таблиці department
        if ("department".equalsIgnoreCase(tableName)) {
            String name = params.get("name");
            String head = params.get("head_of_department");

            if (name == null) throw new Exception("Параметр 'name' обов'язковий!");

            // ЗАХИСТ: Використання PreparedStatement (?)
            String sql = "INSERT INTO department (name, head_of_department) VALUES (?, ?)";
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, name);
                pstmt.setString(2, head); // може бути null
                
                pstmt.executeUpdate();
                return "Успішно додано кафедру: " + name;
            }
        } 
        
        return "Таблиця " + tableName + " поки не підтримується цим парсером.";
    }

    private String handleSecureDelete(String input) throws Exception {
        // Парсинг: delete department(id='5')
        String tableName = extractTableName(input);
        Map<String, String> params = extractParams(input);

        if ("department".equalsIgnoreCase(tableName)) {
            String idStr = params.get("id");
            if (idStr == null) throw new Exception("Параметр 'id' обов'язковий!");

            // ЗАХИСТ: Перевірка на число (Input Validation)
            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                // Якщо хакер ввів "0 OR 1=1", це викличе помилку тут, і запит навіть не піде в БД
                throw new Exception("Спроба злому заблокована! ID має бути числом.");
            }

            // ЗАХИСТ: Параметризований запит
            String sql = "DELETE FROM department WHERE id = ?";
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, id);
                int rows = pstmt.executeUpdate();
                return "Видалено записів: " + rows;
            }
        }

        return "Таблиця " + tableName + " поки не підтримується.";
    }

    // Допоміжний метод: витягує назву таблиці (друге слово)
    private String extractTableName(String input) {
        String[] parts = input.split("[\\s\\(]+"); // спліт по пробілах або дужці
        if (parts.length >= 2) {
            return parts[1];
        }
        return "";
    }

    // Допоміжний метод: витягує параметри з дужок
    private Map<String, String> extractParams(String input) {
        Map<String, String> map = new HashMap<>();
        int start = input.indexOf("(");
        int end = input.lastIndexOf(")");
        if (start != -1 && end != -1) {
            String content = input.substring(start + 1, end); // name='val', id='val'
            String[] pairs = content.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    String key = kv[0].trim();
                    String val = kv[1].trim().replace("'", ""); // прибираємо лапки
                    map.put(key, val);
                }
            }
        }
        return map;
    }

    // Звичайний метод для відображення таблиці (Read)
    private void performRead(String sql, List<String> colNames, List<List<String>> data) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ResultSetMetaData meta = rs.getMetaData();
            int count = meta.getColumnCount();
            for (int i = 1; i <= count; i++) colNames.add(meta.getColumnName(i));
            
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= count; i++) row.add(rs.getString(i));
                data.add(row);
            }
        }
    }
}