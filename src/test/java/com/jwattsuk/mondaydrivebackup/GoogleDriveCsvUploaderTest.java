package com.jwattsuk.mondaydrivebackup;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.testing.auth.oauth2.MockGoogleCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.*;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Create;
import com.google.api.services.drive.model.File;

class GoogleDriveCsvUploaderTest {

    @TempDir
    Path tempDir;

    @Mock
    private Drive mockDriveService;

    @Mock
    private Drive.Files mockFiles;

    @Mock
    private MockGoogleCredential mockGoogleCredential;

    @Mock
    Drive.Files.Create mockCreate;

    private GoogleDriveCsvUploader uploader;
    private String fakeFolderId;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Ensure the mock Drive structure
        when(mockDriveService.files()).thenReturn(mockFiles);
        when(mockFiles.create(any(File.class), any(FileContent.class))).thenReturn(mockCreate);
        when(mockCreate.setFields(anyString())).thenReturn(mockCreate);

        fakeFolderId = "fakeFolderId123";
        mockGoogleCredential = new MockGoogleCredential.Builder().build();
        uploader = new TestableGoogleDriveCsvUploader(mockGoogleCredential, fakeFolderId, mockDriveService);
    }

    @Test
    void testUploadCsvFile_Success() throws IOException {
        // Prepare
        Path csvPath = tempDir.resolve("test.csv");
        String csvContent = "header1,header2\nvalue1,value2";
        Files.writeString(csvPath, csvContent);

        // Set up mock response
        File responseFile = new File()
                .setId("testFileId")
                .setName("test.csv")
                .setParents(Collections.singletonList(fakeFolderId))
                .setMimeType("text/csv");
        when(mockCreate.execute()).thenReturn(responseFile);

        // Execute
        String fileId = uploader.uploadCsvFile(csvPath.toString(), "test.csv");

        // Verify
        assertEquals("testFileId", fileId);

        // Capture and verify the file metadata
        ArgumentCaptor<File> metadataCaptor = ArgumentCaptor.forClass(File.class);
        verify(mockFiles).create(metadataCaptor.capture(), any(FileContent.class));

        File capturedMetadata = metadataCaptor.getValue();
        assertEquals("test.csv", capturedMetadata.getName());
        assertEquals(Collections.singletonList(fakeFolderId), capturedMetadata.getParents());
    }

    @Test
    void testUploadCsvFile_FileNotFound() {
        // Prepare - use a non-existent file path
        String nonExistentPath = tempDir.resolve("non-existent.csv").toString();

        // Execute & Verify
        IOException exception = assertThrows(IOException.class, () -> {
            uploader.uploadCsvFile(nonExistentPath, "test.csv");
        });

        assertTrue(exception.getMessage().contains("CSV file not found"));
    }

    @Test
    void testUploadCsvFile_UploadError() throws IOException {
        // Prepare
        Path csvPath = tempDir.resolve("error.csv");
        Files.writeString(csvPath, "test content");

        // Set up mock to throw an exception
        IOException mockException = new IOException("Drive API error");
        when(mockCreate.execute()).thenThrow(mockException);

        // Execute & Verify
        IOException exception = assertThrows(IOException.class, () -> {
            uploader.uploadCsvFile(csvPath.toString(), "error.csv");
        });

        assertTrue(exception.getMessage().contains("Failed to upload file to Google Drive"));
        assertTrue(exception.getCause().getMessage().contains("Drive API error"));
    }

    /**
     * A testable subclass that allows injecting a mock Drive service and bypasses credential creation
     */
    private static class TestableGoogleDriveCsvUploader extends GoogleDriveCsvUploader {

        private final Drive mockDriveService;

        public TestableGoogleDriveCsvUploader(GoogleCredential mockGoogleCredentials, String driveFolderId, Drive mockDriveService)
                throws IOException, GeneralSecurityException {
            super(mockGoogleCredentials, driveFolderId);
            this.mockDriveService = mockDriveService;
        }

        @Override
        protected Drive getDriveService() {
            return mockDriveService;
        }
    }


}