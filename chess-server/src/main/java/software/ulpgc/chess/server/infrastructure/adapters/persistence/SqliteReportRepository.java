package software.ulpgc.chess.server.infrastructure.adapters.persistence;

import software.ulpgc.chess.core.Color;
import software.ulpgc.chess.server.application.domain.Report;
import software.ulpgc.chess.server.application.ports.ReportRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SqliteReportRepository implements ReportRepository {
    private final SqliteDatabase database;

    public SqliteReportRepository(SqliteDatabase database) {
        this.database = database;
    }

    @Override
    public void save(Report report) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO reports (room_code, game_id, reporter, reported, reason,
                                          reported_time_used_ms, reported_moves, reported_at)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?)""")) {
            statement.setString(1, report.roomCode());
            statement.setString(2, report.gameId());
            statement.setString(3, report.reporter().name());
            statement.setString(4, report.reported().name());
            statement.setString(5, report.reason());
            if (report.reportedTimeUsedMillis() == null) statement.setNull(6, Types.INTEGER);
            else statement.setLong(6, report.reportedTimeUsedMillis());
            statement.setInt(7, report.reportedMoves());
            statement.setString(8, report.reportedAt().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Could not save report of room " + report.roomCode(), e);
        }
    }

    @Override
    public List<Report> findByRoom(String roomCode) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT game_id, reporter, reported, reason, reported_time_used_ms, reported_moves, reported_at
                     FROM reports WHERE room_code = ? ORDER BY id""")) {
            statement.setString(1, roomCode);
            try (ResultSet result = statement.executeQuery()) {
                List<Report> reports = new ArrayList<>();
                while (result.next()) {
                    long timeUsed = result.getLong("reported_time_used_ms");
                    Long reportedTimeUsed = result.wasNull() ? null : timeUsed;
                    reports.add(new Report(
                            roomCode,
                            result.getString("game_id"),
                            Color.valueOf(result.getString("reporter")),
                            Color.valueOf(result.getString("reported")),
                            result.getString("reason"),
                            reportedTimeUsed,
                            result.getInt("reported_moves"),
                            Instant.parse(result.getString("reported_at"))
                    ));
                }
                return reports;
            }
        } catch (SQLException e) {
            throw new PersistenceException("Could not load reports of room " + roomCode, e);
        }
    }
}
