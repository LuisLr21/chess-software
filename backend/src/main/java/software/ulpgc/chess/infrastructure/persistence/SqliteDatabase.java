package software.ulpgc.chess.infrastructure.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteDatabase {
    private final String url;

    public SqliteDatabase(String path) {
        this.url = "jdbc:sqlite:" + path;
        createSchema();
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void createSchema() {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS games (
                        id TEXT PRIMARY KEY,
                        created_at TEXT NOT NULL
                    )""");
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS moves (
                        game_id TEXT NOT NULL REFERENCES games(id),
                        ply INTEGER NOT NULL,
                        from_square TEXT NOT NULL,
                        to_square TEXT NOT NULL,
                        promotion TEXT,
                        played_at TEXT NOT NULL,
                        PRIMARY KEY (game_id, ply)
                    )""");
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS rooms (
                        code TEXT PRIMARY KEY,
                        game_id TEXT NOT NULL REFERENCES games(id),
                        white_player TEXT NOT NULL,
                        black_player TEXT,
                        created_at TEXT NOT NULL
                    )""");
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        room_code TEXT NOT NULL REFERENCES rooms(code),
                        author TEXT NOT NULL,
                        author_name TEXT NOT NULL,
                        text TEXT NOT NULL,
                        sent_at TEXT NOT NULL
                    )""");
            addColumnIfMissing(connection, "games", "ending_status", "TEXT");
            addColumnIfMissing(connection, "games", "ending_winner", "TEXT");
            addColumnIfMissing(connection, "games", "clock_initial_ms", "INTEGER");
            addColumnIfMissing(connection, "games", "clock_white_ms", "INTEGER");
            addColumnIfMissing(connection, "games", "clock_black_ms", "INTEGER");
            addColumnIfMissing(connection, "games", "clock_turn_started_at", "TEXT");
            addColumnIfMissing(connection, "rooms", "white_name", "TEXT");
            addColumnIfMissing(connection, "rooms", "black_name", "TEXT");
            addColumnIfMissing(connection, "rooms", "draw_offered_by", "TEXT");
            addColumnIfMissing(connection, "rooms", "rematch_offered_by", "TEXT");
        } catch (SQLException e) {
            throw new PersistenceException("Could not create the database schema", e);
        }
    }

    private void addColumnIfMissing(Connection connection, String table, String column, String type) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet columns = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (columns.next()) {
                if (columns.getString("name").equals(column)) return;
            }
        }
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
        }
    }
}
