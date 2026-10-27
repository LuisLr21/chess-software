package software.ulpgc.chess.core;

public class IllegalMoveException extends RuntimeException{
    public IllegalMoveException(Square from, Square to) {
        super("Illegal move: " + from + " -> " + to);
    }
}
