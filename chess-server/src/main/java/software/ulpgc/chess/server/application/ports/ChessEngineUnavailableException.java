package software.ulpgc.chess.server.application.ports;

public class ChessEngineUnavailableException extends RuntimeException {
    public ChessEngineUnavailableException(String message) {
        super(message);
    }

    public ChessEngineUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
