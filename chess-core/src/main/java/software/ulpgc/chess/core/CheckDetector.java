package software.ulpgc.chess.core;

import java.util.Map;

public class CheckDetector {
    private final AttackDetector attackDetector = new AttackDetector();

    public boolean isInCheck(Position position, Color color){
        return attackDetector.isAttacked(position, kingSquare(position.board(), color), color.opposite());
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
