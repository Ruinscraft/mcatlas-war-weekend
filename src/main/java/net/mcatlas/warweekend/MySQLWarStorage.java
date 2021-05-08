package net.mcatlas.warweekend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MySQLWarStorage {

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    public MySQLWarStorage(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
    }

    public void createTables() {
        try (Connection connection = createConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS war_weekend_captures (town_name VARCHAR(64), team_name VARCHAR(16), UNIQUE (town_name));");
                statement.execute("CREATE TABLE IF NOT EXISTS war_weekend_kills (team_name VARCHAR(16), kill_count INT, UNIQUE(team_name));");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Void> saveCapture(String townName, String teamName) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = createConnection()) {
                try (PreparedStatement upsert = connection.prepareStatement("INSERT INTO war_weekend_captures (town_name, team_name) VALUES (?, ?) ON DUPLICATE KEY UPDATE team_name = ?;")) {
                    upsert.setString(1, townName);
                    upsert.setString(2, teamName);
                    upsert.setString(3, teamName);
                    upsert.execute();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public class StoredCapture {
        public String townName;
        public String teamName;
    }

    public CompletableFuture<List<StoredCapture>> queryCaptures() {
        return CompletableFuture.supplyAsync(() -> {
            List<StoredCapture> storedCaptures = new ArrayList<>();

            try (Connection connection = createConnection()) {
                try (PreparedStatement query = connection.prepareStatement("SELECT * FROM war_weekend_captures;")) {
                    try (ResultSet result = query.executeQuery()) {
                        while (result.next()) {
                            String townName = result.getString("town_name");
                            String teamName = result.getString("team_name");
                            StoredCapture storedCapture = new StoredCapture();
                            storedCapture.townName = townName;
                            storedCapture.teamName = teamName;
                            storedCaptures.add(storedCapture);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return storedCaptures;
        });
    }

    public CompletableFuture<Void> incrementKillCount(String teamName) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = createConnection()) {
                try (PreparedStatement upsert = connection.prepareStatement("INSERT INTO war_weekend_kills (team_name, kill_count) VALUES (?, ?) ON DUPLICATE KEY UPDATE kill_count = kill_count + 1;")) {
                    upsert.setString(1, teamName);
                    upsert.setInt(2, 1);
                    upsert.execute();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

}
