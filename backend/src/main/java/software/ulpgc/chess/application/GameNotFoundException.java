package software.ulpgc.chess.application;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(String gameId) {
        super("Game not found: " + gameId);
    }
}
