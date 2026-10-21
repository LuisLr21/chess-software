package software.ulpgc.chess.infrastructure.persistence;

import software.ulpgc.chess.application.RoomRepository;
import software.ulpgc.chess.domain.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

public class SqliteRoomRepository implements RoomRepository {
    private final SqliteDatabase database;

    public SqliteRoomRepository(SqliteDatabase database) {
        this.database = database;
    }

    @Override
    public void save(Room room) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO rooms (code, game_id, white_player, black_player, created_at) VALUES (?, ?, ?, ?, ?)
                     ON CONFLICT (code) DO UPDATE SET black_player = excluded.black_player""")) {
            statement.setString(1, room.code());
            statement.setString(2, room.gameId());
            statement.setString(3, room.whitePlayer());
            statement.setString(4, room.blackPlayer());
            statement.setString(5, Instant.now().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Could not save room " + room.code(), e);
        }
    }

    @Override
    public Optional<Room> findByCode(String code) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT code, game_id, white_player, black_player FROM rooms WHERE code = ?")) {
            statement.setString(1, code);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return Optional.empty();
                return Optional.of(new Room(
                        result.getString("code"),
                        result.getString("game_id"),
                        result.getString("white_player"),
                        result.getString("black_player")
                ));
            }
        } catch (SQLException e) {
            throw new PersistenceException("Could not load room " + code, e);
        }
    }
}
