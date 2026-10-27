package software.ulpgc.chess.server.application.usecases;

public class GameNotOverException extends RuntimeException {
    public GameNotOverException() {
        super("The game is still in progress");
    }
}
