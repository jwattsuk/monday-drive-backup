package com.jwattsuk.mondaydrivebackup;

import java.net.http.HttpClient;
import java.util.List;

public class Main {

    private static final String MONDAY_BOARDS = "monday.boards";
    private static final String DRIVE_FOLDER_ID = "google.drive.folderId";
    private static final String MONDAY_API_TOKEN = "MONDAY_API_TOKEN";
    private static final String GOOGLE_CREDENTIALS = "GOOGLE_CREDENTIALS";

    public static void main(String[] args) {
        try {
            AppConfig appConfig = new AppConfig();
            List<AppConfig.Board> boards = appConfig.getMondayBoards();
            String driveFolderId = appConfig.getProperty(DRIVE_FOLDER_ID);
            if (boards == null || boards.isEmpty() || driveFolderId == null) {
                System.err.printf("Error: %s and %s configuration properties must be set%n", MONDAY_BOARDS, DRIVE_FOLDER_ID);
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
            CredentialsBuilder credentialsBuilder = new CredentialsBuilder();
            GoogleDriveCsvUploader driveUploader =
                    new GoogleDriveCsvUploader(credentialsBuilder.set(googleCredentials).build(), driveFolderId);

            // Loop through the boards and export each one
            for (AppConfig.Board board : boards) {
                System.out.println("Exporting Board ID: " + board.getId() + ", Name: " + board.getName());
                // Create the exporter
                MondayToDriveExporter exporter = new MondayToDriveExporter(
                        mondayClient,
                        csvConverter,
                        driveUploader,
                        board
                );

                // Run the export
                String fileId = exporter.exportBoardToGoogleDrive();
                System.out.println("Export completed successfully!");
                System.out.println("Google Drive File ID: " + fileId);
            }


            
        } catch (Exception e) {
            System.err.println("Export failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}