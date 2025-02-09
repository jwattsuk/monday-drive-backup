package com.jwattsuk.mondaydrivebackup;

import java.net.http.HttpClient;

public class Main {
    public static void main(String[] args) {
        try {
            // Get configuration from environment variables
            String mondayApiToken = System.getenv("MONDAY_API_TOKEN");
            String boardId = System.getenv("MONDAY_BOARD_ID");
            
            if (mondayApiToken == null || boardId == null) {
                System.err.println("Error: MONDAY_API_TOKEN and MONDAY_BOARD_ID environment variables must be set");
                System.exit(1);
            }

            // Create components
            HttpClient httpClient = HttpClient.newHttpClient();
            MondayClient mondayClient = new HttpMondayClient(mondayApiToken, httpClient);
            CsvConverter csvConverter = new CsvConverter();
            GoogleDriveCsvUploader driveUploader = new GoogleDriveCsvUploader();

            // Create the exporter
            MondayToDriveExporter exporter = new MondayToDriveExporter(
                mondayClient,
                csvConverter,
                driveUploader,
                boardId
            );

            // Run the export
            String fileId = exporter.exportBoardToGoogleDrive();
            System.out.println("Export completed successfully!");
            System.out.println("Google Drive File ID: " + fileId);
            
        } catch (Exception e) {
            System.err.println("Export failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}