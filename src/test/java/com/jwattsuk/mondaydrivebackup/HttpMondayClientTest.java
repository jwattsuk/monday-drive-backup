package com.jwattsuk.mondaydrivebackup;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// HttpMondayClientTest.java
@ExtendWith(MockitoExtension.class)
class HttpMondayClientTest {
    @Mock
    private HttpClient httpClient;

    @Test
    void fetchBoardData_SuccessfulRequest() throws Exception {
        // Arrange
        String apiToken = "test-token";
        String boardId = "12345";
        HttpMondayClient mondayClient = new HttpMondayClient(apiToken, httpClient);

        String expectedResponse = """
            {
                "data": {
                    "boards": [{
                        "items": [],
                        "columns": []
                    }]
                }
            }
            """;

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn(expectedResponse);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        // Act
        JSONObject result = mondayClient.fetchBoardData(boardId);

        // Assert
        assertNotNull(result);
        assertTrue(result.has("data"));
        verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void fetchBoardData_ThrowsException_WhenHttpClientFails() throws IOException, InterruptedException {
        // Arrange
        String apiToken = "test-token";
        String boardId = "12345";
        HttpMondayClient mondayClient = new HttpMondayClient(apiToken, httpClient);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Network error"));

        // Act & Assert
        assertThrows(IOException.class, () -> mondayClient.fetchBoardData(boardId));
    }
}