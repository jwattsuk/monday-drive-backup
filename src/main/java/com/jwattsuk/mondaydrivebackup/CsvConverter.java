package com.jwattsuk.mondaydrivebackup;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.opencsv.CSVWriter;

public class CsvConverter {
    public String convertToCsv(JSONObject boardData) throws IOException {
        JSONObject board = boardData.getJSONObject("data")
            .getJSONArray("boards")
            .getJSONObject(0);

        return "/test.csv";

        // List<String> headers = extractHeaders(board);
        // List<List<String>> rows = extractRows(board);
        
        // return writeToCsvFile(headers, rows);
    }

    List<String> extractHeaders(JSONObject board) {
        JSONArray columnsData = board.getJSONArray("columns");
        List<String> headers = new ArrayList<>();
        headers.add("Item Name");
        for (int i = 0; i < columnsData.length(); i++) {
            JSONObject column = columnsData.getJSONObject(i);
            headers.add(column.getString("title"));
        }
        return headers;
    }

    List<List<String>> extractRows(JSONObject board) {
        List<List<String>> rows = new ArrayList<>();
        JSONArray items = board.getJSONArray("items");
        
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            List<String> rowData = new ArrayList<>();
            
            rowData.add(item.getString("name"));
            
            JSONArray columnValues = item.getJSONArray("column_values");
            for (int j = 0; j < columnValues.length(); j++) {
                JSONObject columnValue = columnValues.getJSONObject(j);
                String text = columnValue.optString("text", "");
                rowData.add(text);
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
