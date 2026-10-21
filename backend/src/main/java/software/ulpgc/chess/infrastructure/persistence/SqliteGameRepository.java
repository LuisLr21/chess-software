package software.ulpgc.chess.infrastructure.persistence;

import software.ulpgc.chess.application.GameRepository;
import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.Move;
import software.ulpgc.chess.domain.PieceType;
import software.ulpgc.chess.domain.Square;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            try (PreparedStatement insertGame = connection.prepareStatement(
                    "INSERT OR IGNORE INTO games (id, created_at) VALUES (?, ?)")) {
                insertGame.setString(1, game.id());
                insertGame.setString(2, now);
                insertGame.executeUpdate();
            }
            try (PreparedStatement insertMove = connection.prepareStatement(
                    "INSERT OR IGNORE INTO moves (game_id, ply, from_square, to_square, promotion, played_at) VALUES (?, ?, ?, ?, ?, ?)")) {
                List<Move> history = game.history();
                for (int ply = 0; ply < history.size(); ply++) {
                    Move move = history.get(ply);
                    insertMove.setString(1, game.id());
                    insertMove.setInt(2, ply);
                    insertMove.setString(3, move.from().toString());
                    insertMove.setString(4, move.to().toString());
                    insertMove.setString(5, move.promotion() == null ? null : String.valueOf(move.promotion().symbol()));
                    insertMove.setString(6, now);
                    insertMove.executeUpdate();
                }
            }
            connection.commit();
        } catch (SQLException e) {
            throw new PersistenceException("Could not save game " + game.id(), e);
        }
    }

    @Override
    public Optional<Game> findById(String id) {
        try (Connection connection = database.connect()) {
            if (!exists(connection, id)) return Optional.empty();
            return Optional.of(Game.replay(id, movesOf(connection, id)));
        } catch (SQLException e) {
            throw new PersistenceException("Could not load game " + id, e);
        }
    }

    private boolean exists(Connection connection, String id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM games WHERE id = ?")) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
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
}
