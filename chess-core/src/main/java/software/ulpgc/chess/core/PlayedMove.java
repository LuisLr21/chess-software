package software.ulpgc.chess.core;

public record PlayedMove(Move move, String san, Piece captured) {
}
