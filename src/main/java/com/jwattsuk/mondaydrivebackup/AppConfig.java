package com.jwattsuk.mondaydrivebackup;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AppConfig {
    private static final String CONFIG_FILE = "/config.properties";
    private Properties properties;

    public AppConfig() throws IOException {
        properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IOException("Configuration file not found: " + CONFIG_FILE);
            }
            properties.load(input);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public List<Board> getMondayBoards() {
        List<Board> boards = new ArrayList<>();

        // Get the number of boards
        int count = Integer.parseInt(getProperty("monday.boards.count"));

        // Loop through and create board objects
        for (int i = 0; i < count; i++) {
            String idKey = "monday.boards." + i + ".id";
            String nameKey = "monday.boards." + i + ".name";

            long id = Long.parseLong(getProperty(idKey));
            String name = getProperty(nameKey);

            boards.add(new Board(id, name));
        }

        return boards;
    }

    // Simple Board class to hold the properties
    public static class Board {
        private final long id;
        private final String name;

        public Board(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Board{id=" + id + ", name='" + name + "'}";
        }
    }
}
