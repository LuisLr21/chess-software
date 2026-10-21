package software.ulpgc.chess.infrastructure.persistence;

import software.ulpgc.chess.application.RoomRepository;
import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Room;
import software.ulpgc.chess.domain.Seat;

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
                     INSERT INTO rooms (code, game_id, white_player, white_name, black_player, black_name,
                                        draw_offered_by, rematch_offered_by, created_at)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                     ON CONFLICT (code) DO UPDATE SET
                         game_id = excluded.game_id,
                         white_player = excluded.white_player, white_name = excluded.white_name,
                         black_player = excluded.black_player, black_name = excluded.black_name,
                         draw_offered_by = excluded.draw_offered_by, rematch_offered_by = excluded.rematch_offered_by""")) {
            statement.setString(1, room.code());
            statement.setString(2, room.gameId());
            statement.setString(3, room.white().player());
            statement.setString(4, room.white().name());
            statement.setString(5, room.isFull() ? room.black().player() : null);
            statement.setString(6, room.isFull() ? room.black().name() : null);
            statement.setString(7, name(room.drawOfferedBy()));
            statement.setString(8, name(room.rematchOfferedBy()));
            statement.setString(9, Instant.now().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Could not save room " + room.code(), e);
        }
    }

    @Override
    public Optional<Room> findByCode(String code) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT code, game_id, white_player, white_name, black_player, black_name, draw_offered_by, rematch_offered_by
                     FROM rooms WHERE code = ?""")) {
            statement.setString(1, code);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return Optional.empty();
                String blackPlayer = result.getString("black_player");
                return Optional.of(new Room(
                        result.getString("code"),
                        result.getString("game_id"),
                        Seat.of(result.getString("white_player"), result.getString("white_name"), "Blancas"),
                        blackPlayer == null ? null : Seat.of(blackPlayer, result.getString("black_name"), "Negras"),
                        color(result.getString("draw_offered_by")),
                        color(result.getString("rematch_offered_by"))
                ));
            }
        } catch (SQLException e) {
            throw new PersistenceException("Could not load room " + code, e);
        }
    }

    private static String name(Color color) {
        return color == null ? null : color.name();
    }

    private static Color color(String name) {
        return name == null ? null : Color.valueOf(name);
    }
}
