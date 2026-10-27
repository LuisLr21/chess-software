package software.ulpgc.chess.server.application.usecases;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(String gameId) {
        super("Game not found: " + gameId);
    }
}
