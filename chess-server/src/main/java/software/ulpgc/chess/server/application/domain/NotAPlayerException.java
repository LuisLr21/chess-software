package software.ulpgc.chess.server.application.domain;

public class NotAPlayerException extends RuntimeException {
    public NotAPlayerException(String code) {
        super("You are not a player of room " + code);
    }
}
