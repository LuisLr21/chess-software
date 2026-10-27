package software.ulpgc.chess.server.application.ports;

import software.ulpgc.chess.server.application.domain.Report;

import java.util.List;

public interface ReportRepository {
    void save(Report report);
    List<Report> findByRoom(String roomCode);
}
