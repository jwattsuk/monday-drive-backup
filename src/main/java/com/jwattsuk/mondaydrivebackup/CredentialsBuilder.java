package com.jwattsuk.mondaydrivebackup;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.drive.DriveScopes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Base64;

public class CredentialsBuilder extends GoogleCredential {

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private String encodedCredentials;

    public CredentialsBuilder set(String encodedCredentials) {
        this.encodedCredentials = encodedCredentials;
        return this;
    }

    public GoogleCredential build() throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedCredentials);
        InputStream credentialsStream = new ByteArrayInputStream(decodedBytes);
        return GoogleCredential.fromStream(credentialsStream).createScoped(SCOPES);
    }
}
