package com.jwattsuk.mondaydrivebackup;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// MondayToDriveExporterTest.java
@ExtendWith(MockitoExtension.class)
class MondayToDriveExporterTest {
    @Mock
    private MondayClient mondayClient;
    @Mock
    private CsvConverter csvConverter;
    @Mock
    private GoogleDriveCsvUploader driveUploader;
    @Mock
    private AppConfig.Board board;

    private MondayToDriveExporter exporter;

    @BeforeEach
    void setUp() {
        exporter = new MondayToDriveExporter(
                mondayClient,
                csvConverter,
                driveUploader,
                board
        );
    }

    @Test
    void exportBoardToGoogleDrive_SuccessfulExport() throws Exception {
        // Arrange
        JSONObject mockBoardData = new JSONObject();
        String mockCsvPath = "test.csv";
        String mockFileId = "drive-file-id";

        when(mondayClient.fetchBoardData("123456")).thenReturn(mockBoardData);
        when(csvConverter.convertToCsv(mockBoardData)).thenReturn(mockCsvPath);
        when(driveUploader.uploadCsvFile(eq(mockCsvPath), any())).thenReturn(mockFileId);
        when(board.getId()).thenReturn(123456L);

        // Act
        String result = exporter.exportBoardToGoogleDrive();

        // Assert
        assertEquals(mockFileId, result);
        verify(mondayClient).fetchBoardData("123456");
        verify(csvConverter).convertToCsv(mockBoardData);
        verify(driveUploader).uploadCsvFile(eq(mockCsvPath), any());
    }

    @Test
    void exportBoardToGoogleDrive_CleanupsCsvFile_OnSuccess() throws Exception {
        // Arrange
        String tempFilePath = "test.csv";
        File tempFile = new File(tempFilePath);
        tempFile.createNewFile();

        when(mondayClient.fetchBoardData(any())).thenReturn(new JSONObject());
        when(csvConverter.convertToCsv(any())).thenReturn(tempFilePath);
        when(driveUploader.uploadCsvFile(any(), any())).thenReturn("file-id");

        // Act
        exporter.exportBoardToGoogleDrive();

        // Assert
        assertFalse(tempFile.exists());
    }

    @Test
    void exportBoardToGoogleDrive_CleanupsCsvFile_OnError() throws Exception {
        // Arrange
        String tempFilePath = "test.csv";
        File tempFile = new File(tempFilePath);
        tempFile.createNewFile();

        when(mondayClient.fetchBoardData(any())).thenReturn(new JSONObject());
        when(csvConverter.convertToCsv(any())).thenReturn(tempFilePath);
        when(driveUploader.uploadCsvFile(any(), any()))
                .thenThrow(new IOException("Upload failed"));

        // Act & Assert
        assertThrows(IOException.class, () -> exporter.exportBoardToGoogleDrive());
        assertFalse(tempFile.exists());
    }
}
