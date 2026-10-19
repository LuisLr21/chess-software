package software.ulpgc.chess;

public interface MovementRule {
    boolean canMove(Board board, Square from, Square to);

    default boolean isOccupiedBySameColor(Board board, Square square, Color color){
        Piece piece = board.pieceAt(square);
        return piece != null && piece.color() == color;
    }
}
