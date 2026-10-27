package software.ulpgc.chess.server.infrastructure.adapters.http;

import software.ulpgc.chess.server.application.domain.Report;

public record ReportResponse(String gameId, String reporter, String reported, String reason,
                             Long reportedTimeUsedMillis, int reportedMoves, String reportedAt) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(report.gameId(), report.reporter().name(), report.reported().name(), report.reason(),
                report.reportedTimeUsedMillis(), report.reportedMoves(), report.reportedAt().toString());
    }
}
