package software.ulpgc.chess.core;

public class GameOverException extends RuntimeException {
    public GameOverException(GameStatus status) {
        super("Game is over: " + status);
    }
}
