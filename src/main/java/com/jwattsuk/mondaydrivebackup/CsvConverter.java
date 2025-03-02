package com.jwattsuk.mondaydrivebackup;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.opencsv.CSVWriter;

public class CsvConverter {
    public String convertToCsv(JSONObject boardData) throws IOException {
        JSONObject board = boardData.getJSONObject("data")
            .getJSONArray("boards")
            .getJSONObject(0);

        List<String> headers = extractHeaders(board);
        List<List<String>> rows = extractRows(board, headers);
        
        return writeToCsvFile(headers, rows);
    }

    List<String> extractHeaders(JSONObject board) {
        List<String> headers = new ArrayList<>();
        headers.add("Item Name");

        // Check if "items_page" -> "items" exists
        if (!board.has("items_page") || board.isNull("items_page")) return headers;
        JSONObject itemsPage = board.getJSONObject("items_page");

        if (!itemsPage.has("items") || itemsPage.isNull("items")) return headers;
        JSONArray items = itemsPage.getJSONArray("items");

        // Use a set to avoid duplicate headers
        Set<String> uniqueHeaders = new LinkedHashSet<>();

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (!item.has("column_values") || item.isNull("column_values")) continue;

            JSONArray columnValues = item.getJSONArray("column_values");
            for (int j = 0; j < columnValues.length(); j++) {
                JSONObject columnValue = columnValues.getJSONObject(j);
                if (!columnValue.has("column") || columnValue.isNull("column")) continue;

                JSONObject column = columnValue.getJSONObject("column");
                String title = column.optString("title", "Unknown Column");
                uniqueHeaders.add(title);
            }
        }

        headers.addAll(uniqueHeaders);
        return headers;
    }

    List<List<String>> extractRows(JSONObject board, List<String> headers) {
        List<List<String>> rows = new ArrayList<>();

        // Navigate to "items_page" -> "items"
        if (!board.has("items_page") || board.isNull("items_page")) return rows;
        JSONObject itemsPage = board.getJSONObject("items_page");

        if (!itemsPage.has("items") || itemsPage.isNull("items")) return rows;
        JSONArray items = itemsPage.getJSONArray("items");

        // Process each row (item)
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            List<String> rowData = new ArrayList<>(Collections.nCopies(headers.size(), ""));

            // Set "Item Name" column
            rowData.set(0, item.optString("name", "Unnamed Item"));

            if (!item.has("column_values") || item.isNull("column_values")) {
                rows.add(rowData);
                continue;
            }

            JSONArray columnValues = item.getJSONArray("column_values");

            // Map column titles to values
            Map<String, String> columnMap = new HashMap<>();
            for (int j = 0; j < columnValues.length(); j++) {
                JSONObject columnValue = columnValues.getJSONObject(j);
                if (!columnValue.has("column") || columnValue.isNull("column")) continue;

                JSONObject column = columnValue.getJSONObject("column");
                String title = column.optString("title", "Unknown Column");
                String text = columnValue.optString("text", "");

                columnMap.put(title, text);
            }

            // Populate row based on headers
            for (int j = 1; j < headers.size(); j++) {
                rowData.set(j, columnMap.getOrDefault(headers.get(j), ""));
            }

            rows.add(rowData);
        }

        return rows;
    }

    private String writeToCsvFile(List<String> headers, List<List<String>> rows) throws IOException {
        String tempFile = "board_export_" + System.currentTimeMillis() + ".csv";
        
        try (CSVWriter writer = new CSVWriter(new FileWriter(tempFile))) {
            writer.writeNext(headers.toArray(new String[0]));
            
            for (List<String> row : rows) {
                writer.writeNext(row.toArray(new String[0]));
            }
        }
        
        return tempFile;
    }
}
