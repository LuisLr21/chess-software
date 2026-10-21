package software.ulpgc.chess.application;

public class GameNotOverException extends RuntimeException {
    public GameNotOverException() {
        super("The game is still in progress");
    }
}
