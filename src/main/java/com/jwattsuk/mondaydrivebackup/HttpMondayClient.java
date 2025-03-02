package com.jwattsuk.mondaydrivebackup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;



// HttpMondayClient.java
public class HttpMondayClient implements MondayClient {
    private static final String MONDAY_API_URL = "https://api.monday.com/v2";
    private final String apiToken;
    private final HttpClient httpClient;

    public HttpMondayClient(String apiToken, HttpClient httpClient) {
        this.apiToken = apiToken;
        this.httpClient = httpClient;
    }

    @Override
    public JSONObject fetchBoardData(String boardId) throws IOException, InterruptedException {
        String query = loadBoardQuery(boardId);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(MONDAY_API_URL))
            .header("Authorization", apiToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(
                new JSONObject().put("query", query).toString()))
            .build();

        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());

        return new JSONObject(response.body());
    }

    private String loadBoardQuery(String boardId) {
        String filePath = "/" + boardId + ".txt";
        StringBuilder content = new StringBuilder();

        try (InputStream inputStream = getClass().getResourceAsStream(filePath)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }

                return content.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load query for board " + boardId, e);
        }
    }
}