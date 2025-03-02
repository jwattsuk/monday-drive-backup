package com.jwattsuk.mondaydrivebackup;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// CsvConverterTest.java
class CsvConverterTest {
    private CsvConverter csvConverter;
    private JSONObject board;

    @BeforeEach
    void setUp() {
        csvConverter = new CsvConverter();
        board = new JSONObject("""
                {
                  "items_page": {
                    "items": [
                      {
                        "column_values": [
                          {
                            "column": {
                              "title": "Area"
                            },
                            "id": "status",
                            "text": "Sefton",
                            "value": "{\\"index\\":0,\\"post_id\\":null,\\"changed_at\\":\\"2025-02-16T13:05:10.032Z\\"}"
                          },
                          {
                            "column": {
                              "title": "Stage"
                            },
                            "id": "status_1_Mjj12aJK",
                            "text": "Mentor groups and bag delivery",
                            "value": "{\\"index\\":2,\\"post_id\\":null,\\"changed_at\\":\\"2025-02-26T09:46:57.694Z\\"}"
                          }
                        ],
                        "name": "Sofia"
                      }
                    ]
                  },
                  "name": "Participants",
                  "description": null,
                  "id": "1734380575"
                }
                """);
    }

    @Test
    void extractHeaders_ReturnsCorrectHeaders() {
        // Arrange

        // Act
        List<String> headers = csvConverter.extractHeaders(board);

        // Assert
        assertEquals(Arrays.asList("Item Name", "Area", "Stage"), headers);
    }

    @Test
    void extractRows_ReturnsCorrectRows() {
        // Arrange

        // Act
        List<List<String>> rows = csvConverter.extractRows(board, Arrays.asList("Item Name", "Area", "Stage"));

        // Assert
        assertEquals(1, rows.size());
        assertEquals(Arrays.asList("Sofia", "Sefton", "Mentor groups and bag delivery"), rows.get(0));
    }

    @Test
    void convertToCsv_CreatesCsvFile() throws IOException {
        // Arrange
        JSONObject boardData = new JSONObject("""
            {
                "data": {
                    "boards": [{
                        "columns": [
                            {"title": "Status", "type": "status"}
                        ],
                        "items": [
                            {
                                "name": "Task 1",
                                "column_values": [
                                    {"title": "Status", "text": "Done"}
                                ]
                            }
                        ]
                    }]
                }
            }
            """);

        // Act
        String filePath = csvConverter.convertToCsv(boardData);

        // Assert
        File csvFile = new File(filePath);
        assertTrue(csvFile.exists());

        // Clean up
        csvFile.delete();
    }
}
