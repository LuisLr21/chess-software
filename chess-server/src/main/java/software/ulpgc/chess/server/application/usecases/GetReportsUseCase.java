package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.domain.Report;
import software.ulpgc.chess.server.application.ports.ReportRepository;
import software.ulpgc.chess.server.application.ports.RoomRepository;

import java.util.List;

public class GetReportsUseCase implements UseCase<String, List<Report>> {
    private final RoomRepository rooms;
    private final ReportRepository reports;

    public GetReportsUseCase(RoomRepository rooms, ReportRepository reports) {
        this.rooms = rooms;
        this.reports = reports;
    }

    @Override
    public List<Report> execute(String code) {
        if (rooms.findByCode(code).isEmpty()) throw new RoomNotFoundException(code);
        return reports.findByRoom(code);
    }
}
