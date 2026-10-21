package software.ulpgc.chess.infrastructure.persistence;

import software.ulpgc.chess.application.GameRepository;
import software.ulpgc.chess.domain.ChessClock;
import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Ending;
import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.GameStatus;
import software.ulpgc.chess.domain.Move;
import software.ulpgc.chess.domain.PieceType;
import software.ulpgc.chess.domain.Square;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteGameRepository implements GameRepository {
    private final SqliteDatabase database;

    public SqliteGameRepository(SqliteDatabase database) {
        this.database = database;
    }

    @Override
    public void save(Game game) {
        try (Connection connection = database.connect()) {
            connection.setAutoCommit(false);
            String now = Instant.now().toString();
            saveGameRow(connection, game, now);
            saveMoves(connection, game, now);
            connection.commit();
        } catch (SQLException e) {
            throw new PersistenceException("Could not save game " + game.id(), e);
        }
    }

    @Override
    public Optional<Game> findById(String id) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT ending_status, ending_winner, clock_initial_ms, clock_white_ms, clock_black_ms, clock_turn_started_at
                     FROM games WHERE id = ?""")) {
            statement.setString(1, id);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) return Optional.empty();
                return Optional.of(Game.restore(id, movesOf(connection, id), ending(row), clock(row)));
            }
        } catch (SQLException e) {
            throw new PersistenceException("Could not load game " + id, e);
        }
    }

    private void saveGameRow(Connection connection, Game game, String now) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO games (id, created_at, ending_status, ending_winner,
                                   clock_initial_ms, clock_white_ms, clock_black_ms, clock_turn_started_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    ending_status = excluded.ending_status, ending_winner = excluded.ending_winner,
                    clock_initial_ms = excluded.clock_initial_ms, clock_white_ms = excluded.clock_white_ms,
                    clock_black_ms = excluded.clock_black_ms, clock_turn_started_at = excluded.clock_turn_started_at""")) {
            Ending ending = game.ending();
            ChessClock clock = game.clock();
            statement.setString(1, game.id());
            statement.setString(2, now);
            statement.setString(3, ending == null ? null : ending.status().name());
            statement.setString(4, ending == null || ending.winner() == null ? null : ending.winner().name());
            setLong(statement, 5, clock == null ? null : clock.initialMillis());
            setLong(statement, 6, clock == null ? null : clock.whiteMillis());
            setLong(statement, 7, clock == null ? null : clock.blackMillis());
            statement.setString(8, clock == null || clock.turnStartedAt() == null ? null : clock.turnStartedAt().toString());
            statement.executeUpdate();
        }
    }

    private void saveMoves(Connection connection, Game game, String now) throws SQLException {
        try (PreparedStatement insertMove = connection.prepareStatement(
                "INSERT OR IGNORE INTO moves (game_id, ply, from_square, to_square, promotion, played_at) VALUES (?, ?, ?, ?, ?, ?)")) {
            List<Move> moves = game.moves();
            for (int ply = 0; ply < moves.size(); ply++) {
                Move move = moves.get(ply);
                insertMove.setString(1, game.id());
                insertMove.setInt(2, ply);
                insertMove.setString(3, move.from().toString());
                insertMove.setString(4, move.to().toString());
                insertMove.setString(5, move.promotion() == null ? null : String.valueOf(move.promotion().symbol()));
                insertMove.setString(6, now);
                insertMove.executeUpdate();
            }
        }
    }

    private List<Move> movesOf(Connection connection, String id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT from_square, to_square, promotion FROM moves WHERE game_id = ? ORDER BY ply")) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                List<Move> moves = new ArrayList<>();
                while (result.next()) {
                    String promotion = result.getString("promotion");
                    moves.add(new Move(
                            Square.at(result.getString("from_square")),
                            Square.at(result.getString("to_square")),
                            promotion == null ? null : PieceType.fromSymbol(promotion.charAt(0))
                    ));
                }
                return moves;
            }
        }
    }

    private static Ending ending(ResultSet row) throws SQLException {
        String status = row.getString("ending_status");
        if (status == null) return null;
        String winner = row.getString("ending_winner");
        return new Ending(GameStatus.valueOf(status), winner == null ? null : Color.valueOf(winner));
    }

    private static ChessClock clock(ResultSet row) throws SQLException {
        long initial = row.getLong("clock_initial_ms");
        if (row.wasNull()) return null;
        String startedAt = row.getString("clock_turn_started_at");
        return new ChessClock(initial, row.getLong("clock_white_ms"), row.getLong("clock_black_ms"),
                startedAt == null ? null : Instant.parse(startedAt));
    }

    private static void setLong(PreparedStatement statement, int index, Long value) throws SQLException {
        if (value == null) statement.setNull(index, Types.INTEGER);
        else statement.setLong(index, value);
    }
}
