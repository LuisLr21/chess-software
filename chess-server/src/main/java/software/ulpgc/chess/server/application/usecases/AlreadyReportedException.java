package software.ulpgc.chess.server.application.usecases;

public class AlreadyReportedException extends RuntimeException {
    public AlreadyReportedException() {
        super("You already reported your opponent in this game");
    }
}
