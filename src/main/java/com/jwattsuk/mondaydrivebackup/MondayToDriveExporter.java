package com.jwattsuk.mondaydrivebackup;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;

public class MondayToDriveExporter {
    private final MondayClient mondayClient;
    private final CsvConverter csvConverter;
    private final GoogleDriveCsvUploader driveUploader;
    private final AppConfig.Board board;

    public MondayToDriveExporter(
            MondayClient mondayClient,
            CsvConverter csvConverter,
            GoogleDriveCsvUploader driveUploader,
            AppConfig.Board board) {
        this.mondayClient = mondayClient;
        this.csvConverter = csvConverter;
        this.driveUploader = driveUploader;
        this.board = board;
    }

    public String exportBoardToGoogleDrive() throws Exception {
        JSONObject boardData = mondayClient.fetchBoardData(String.valueOf(board.getId()));
        String csvFilePath = csvConverter.convertToCsv(boardData);
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fileName = "monday_board_" + board.getName() + "_" + timestamp + ".csv";
        
        try {
            return driveUploader.uploadCsvFile(csvFilePath, fileName);
        } finally {
            new File(csvFilePath).delete();
        }
    }
}