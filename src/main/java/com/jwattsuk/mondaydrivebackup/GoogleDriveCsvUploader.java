package com.jwattsuk.mondaydrivebackup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

public class GoogleDriveCsvUploader {
    private static final String APPLICATION_NAME = "Monday.com Board Backup";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    private final GoogleCredential googleCredentials;
    private final String driveFolderId;
    private Drive driveService;

    public GoogleDriveCsvUploader(GoogleCredential googleCredentials, String driveFolderId) throws IOException, GeneralSecurityException {
        this.googleCredentials = googleCredentials;
        this.driveFolderId = driveFolderId;
        this.driveService = initializeDriveService();
    }

    /**
     * Initializes the Drive service.
     * This method is separated to allow for testing.
     */
    private Drive initializeDriveService() throws IOException, GeneralSecurityException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(httpTransport, JSON_FACTORY, googleCredentials)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Gets the Drive service.
     * This protected method allows for testing by being overridden.
     */
    protected Drive getDriveService() {
        return driveService;
    }

    /**
     * Uploads a CSV file to Google Drive.
     *
     * @param filePath Local path to the CSV file
     * @param fileName Name to give the file in Google Drive
     * @return ID of the uploaded file
     * @throws IOException If there's an error uploading the file
     */
    public String uploadCsvFile(String filePath, String fileName) throws IOException {
        // Create file metadata
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(driveFolderId));

        java.io.File csvFile = new java.io.File(filePath);
        if (!csvFile.exists()) {
            throw new IOException("CSV file not found: " + filePath);
        }

        FileContent mediaContent = new FileContent("text/csv", csvFile);

        try {
            // Upload file
            File uploadedFile = getDriveService().files().create(fileMetadata, mediaContent)
                    .setFields("id, name, parents, mimeType")
                    .execute();

            if (uploadedFile != null) {
                System.out.println("✅ File Uploaded Successfully!");
                System.out.println("📂 File ID: " + uploadedFile.getId());
                System.out.println("📄 File Name: " + uploadedFile.getName());
                System.out.println("📁 Parent Folder(s): " + uploadedFile.getParents());
                System.out.println("📜 MIME Type: " + uploadedFile.getMimeType());
            } else {
                System.out.println("❌ File upload failed.");
            }
            return uploadedFile.getId();
        } catch (IOException e) {
            throw new IOException("Failed to upload file to Google Drive: " + e.getMessage(), e);
        }
    }
}