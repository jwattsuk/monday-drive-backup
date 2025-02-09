package com.jwattsuk.mondaydrivebackup;

import java.io.IOException;

import org.json.JSONObject;

public interface MondayClient {
    JSONObject fetchBoardData(String boardId) throws IOException, InterruptedException;
} 