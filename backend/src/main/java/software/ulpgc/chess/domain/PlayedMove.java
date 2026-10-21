package software.ulpgc.chess.domain;

public record PlayedMove(Move move, String san, Piece captured) {
}
