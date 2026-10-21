package software.ulpgc.chess.domain;

public class WaitingForOpponentException extends RuntimeException {
    public WaitingForOpponentException(String code) {
        super("Waiting for an opponent to join room " + code);
    }
}
