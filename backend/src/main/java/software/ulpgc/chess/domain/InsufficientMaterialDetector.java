package software.ulpgc.chess.domain;

import java.util.List;
import java.util.Map;

public class InsufficientMaterialDetector {

    public boolean isInsufficient(Board board) {
        List<Map.Entry<Square, Piece>> others = board.pieces().entrySet().stream()
                .filter(entry -> entry.getValue().type() != PieceType.King)
                .toList();
        if (others.isEmpty()) return true;
        if (others.size() == 1) return isMinorPiece(others.getFirst().getValue());
        return others.stream().allMatch(entry -> entry.getValue().type() == PieceType.Bishop)
                && others.stream().map(entry -> isLightSquare(entry.getKey())).distinct().count() == 1;
    }

    private boolean isMinorPiece(Piece piece) {
        return piece.type() == PieceType.Bishop || piece.type() == PieceType.Knight;
    }

    private boolean isLightSquare(Square square) {
        return (square.file().ordinal() + square.rank().value()) % 2 == 0;
    }
}
