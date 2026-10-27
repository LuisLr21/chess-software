package software.ulpgc.chess.server.infrastructure.adapters.persistence;

public class PersistenceException extends RuntimeException {
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
