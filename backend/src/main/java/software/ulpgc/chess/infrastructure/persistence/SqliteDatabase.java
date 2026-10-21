package software.ulpgc.chess.infrastructure.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
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
        } catch (SQLException e) {
            throw new PersistenceException("Could not create the database schema", e);
        }
    }
}
