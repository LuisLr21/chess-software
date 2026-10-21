package software.ulpgc.chess.domain;

public class GameOverException extends RuntimeException {
    public GameOverException(GameStatus status) {
        super("Game is over: " + status);
    }
}
