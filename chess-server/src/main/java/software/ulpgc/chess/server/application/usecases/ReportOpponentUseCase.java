package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.core.Color;
import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.domain.Report;
import software.ulpgc.chess.server.application.domain.Room;
import software.ulpgc.chess.server.application.ports.ReportRepository;
import software.ulpgc.chess.server.application.ports.RoomNotifier;

import java.time.Clock;

public class ReportOpponentUseCase implements UseCase<NewReport, Report> {
    private final ReportRepository reports;
    private final GetRoomUseCase getRoom;
    private final RoomNotifier notifier;
    private final Clock clock;

    public ReportOpponentUseCase(ReportRepository reports, GetRoomUseCase getRoom, RoomNotifier notifier, Clock clock) {
        this.reports = reports;
        this.getRoom = getRoom;
        this.notifier = notifier;
        this.clock = clock;
    }

    @Override
    public synchronized Report execute(NewReport newReport) {
        String code = newReport.code();
        RoomState state = getRoom.execute(code);
        Room room = state.room();
        room.ensureStarted();
        Color reporter = room.playerColor(newReport.player());
        Game game = state.game();
        if (alreadyReported(code, game.id(), reporter)) throw new AlreadyReportedException();
        Report report = Report.of(code, game, reporter, newReport.reason(), clock.instant());
        reports.save(report);
        notifier.reportFiled(report);
        return report;
    }

    private boolean alreadyReported(String code, String gameId, Color reporter) {
        return reports.findByRoom(code).stream()
                .anyMatch(report -> report.gameId().equals(gameId) && report.reporter() == reporter);
    }
}
