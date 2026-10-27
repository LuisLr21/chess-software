package software.ulpgc.chess.server.infrastructure.adapters.persistence;

import software.ulpgc.chess.server.application.domain.Report;
import software.ulpgc.chess.server.application.ports.ReportRepository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryReportRepository implements ReportRepository {
    private final List<Report> reports = new CopyOnWriteArrayList<>();

    @Override
    public void save(Report report) {
        reports.add(report);
    }

    @Override
    public List<Report> findByRoom(String roomCode) {
        return reports.stream().filter(report -> report.roomCode().equals(roomCode)).toList();
    }
}
