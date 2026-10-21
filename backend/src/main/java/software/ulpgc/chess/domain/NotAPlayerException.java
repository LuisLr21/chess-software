package software.ulpgc.chess.domain;

public class NotAPlayerException extends RuntimeException {
    public NotAPlayerException(String code) {
        super("You are not a player of room " + code);
    }
}
