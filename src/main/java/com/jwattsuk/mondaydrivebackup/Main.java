package com.jwattsuk.mondaydrivebackup;

import java.net.http.HttpClient;

public class Main {

    private static final String MONDAY_BOARD_ID = "monday.board_id";
    private static final String DRIVE_FOLDER_ID = "google.drive_folder_id";
    private static final String MONDAY_API_TOKEN = "MONDAY_API_TOKEN";
    private static final String GOOGLE_CREDENTIALS = "GOOGLE_CREDENTIALS";

    public static void main(String[] args) {
        try {
            AppConfig config = new AppConfig();
            String boardId = config.getProperty(MONDAY_BOARD_ID);
            String driveFolderId = config.getProperty(DRIVE_FOLDER_ID);
            if (boardId == null || driveFolderId == null) {
                System.err.printf("Error: %s and %s configuration properties must be set%n", MONDAY_BOARD_ID, DRIVE_FOLDER_ID);
                System.exit(1);
            }

            String mondayApiToken = System.getenv(MONDAY_API_TOKEN);
            String googleCredentials = System.getenv(GOOGLE_CREDENTIALS);
            if (mondayApiToken == null || googleCredentials == null) {
                System.err.println("Error: MONDAY_API_TOKEN and GOOGLE_CREDENTIALS environment variables must be set");
                System.exit(1);
            }

            // Create components
            HttpClient httpClient = HttpClient.newHttpClient();
            MondayClient mondayClient = new HttpMondayClient(mondayApiToken, httpClient);
            CsvConverter csvConverter = new CsvConverter();
            GoogleDriveCsvUploader driveUploader = new GoogleDriveCsvUploader(googleCredentials, driveFolderId);

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