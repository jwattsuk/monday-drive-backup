package com.jwattsuk.mondaydrivebackup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
    private static final String SERVICE_ACCOUNT_KEY_FILE_PATH = "/google-credentials.json";

    private final Drive driveService;
    private final String googleCredentials;
    private final String driveFolderId;

    public GoogleDriveCsvUploader(String googleCredentials, String driveFolderId) throws IOException, GeneralSecurityException {
        this.googleCredentials = googleCredentials;
        this.driveFolderId = driveFolderId;
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        GoogleCredential credentials = getCredentials(httpTransport);
        this.driveService = new Drive.Builder(httpTransport, JSON_FACTORY, credentials)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Creates an authorized Credential object using a service account.
     *
     * @param httpTransport The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the service account key file cannot be found.
     */
    private GoogleCredential getCredentials(final NetHttpTransport httpTransport) throws IOException {
        // Decode the Base64 string
        byte[] decodedBytes = Base64.getDecoder().decode(googleCredentials);
        InputStream credentialsStream = new ByteArrayInputStream(decodedBytes);
        return GoogleCredential.fromStream(credentialsStream).createScoped(SCOPES);
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
        
        // Create file content
        //java.io.File csvFile = new java.io.File(filePath);

        // Load CSV file from resources
        InputStream csvStream = getClass().getResourceAsStream(filePath);
        if (csvStream == null) {
            throw new IOException("CSV file not found: " + filePath);
        }

        // Create a temporary file
        java.io.File tempFile = java.io.File.createTempFile("temp", ".csv");
        Files.copy(csvStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Use the temporary file for uploading
        java.io.File csvFile = tempFile;

        FileContent mediaContent = new FileContent("text/csv", csvFile);

        try {
            // Upload file
            File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
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