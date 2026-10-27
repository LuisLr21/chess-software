package software.ulpgc.chess.server.application.domain;

public class WaitingForOpponentException extends RuntimeException {
    public WaitingForOpponentException(String code) {
        super("Waiting for an opponent to join room " + code);
    }
}
