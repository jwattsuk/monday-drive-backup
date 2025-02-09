package com.jwattsuk.mondaydrivebackup;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
        String query = """
            query {
                boards(ids: %s) {
                    items {
                        name
                        column_values {
                            title
                            text
                            value
                        }
                    }
                    columns {
                        title
                        type
                    }
                }
            }
        """.formatted(boardId);

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
}