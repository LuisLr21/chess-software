package software.ulpgc.chess;

import java.util.Map;

public class CheckDetector {
    private final MovementRule rules = new MovementRules();

    public boolean isInCheck(Position position, Color color){
        Square kingSquare = kingSquare(position.board(), color);
        for (Map.Entry<Square, Piece> entry : position.board().pieces().entrySet()) {
            if (entry.getValue().color() == color.opposite()
                    && rules.canMove(position, entry.getKey(), kingSquare)) {
                return true;
            }
        }
        return false;
    }

    private Square kingSquare(Board board, Color color) {
        Piece king = Piece.of(color, PieceType.King);
        for (Map.Entry<Square, Piece> entry : board.pieces().entrySet()) {
            if (entry.getValue() == king) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("No king of color " + color);
    }
}
