package software.ulpgc.chess.core;

import java.util.Map;

public class AttackDetector {
    private final MovementRule rules = new MovementRules();

    public boolean isAttacked(Position position, Square square, Color attacker) {
        for (Map.Entry<Square, Piece> entry : position.board().pieces().entrySet()) {
            Piece piece = entry.getValue();
            if (piece.color() == attacker && attacks(position, entry.getKey(), piece, square)) {
                return true;
            }
        }
        return false;
    }

    private boolean attacks(Position position, Square from, Piece piece, Square target) {
        if (piece.type() == PieceType.Pawn) return pawnAttacks(from, piece.color(), target);
        return rules.canMove(position, from, target);
    }

    private boolean pawnAttacks(Square from, Color color, Square target) {
        int direction = color == Color.White ? 1 : -1;
        int fileDiff = Math.abs(target.file().ordinal() - from.file().ordinal());
        int rankDiff = target.rank().value() - from.rank().value();
        return fileDiff == 1 && rankDiff == direction;
    }
}
