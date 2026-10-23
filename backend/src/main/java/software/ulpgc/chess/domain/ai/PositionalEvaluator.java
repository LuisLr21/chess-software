package software.ulpgc.chess.domain.ai;

import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Piece;
import software.ulpgc.chess.domain.PieceType;
import software.ulpgc.chess.domain.Position;
import software.ulpgc.chess.domain.Square;

import java.util.Map;

public class PositionalEvaluator {
    private static final int ENDGAME_MATERIAL = 1600;

    public int evaluate(Position position, Color color) {
        Map<Square, Piece> pieces = position.board().pieces();
        boolean endgame = isEndgame(pieces);
        int score = 0;
        for (Map.Entry<Square, Piece> entry : pieces.entrySet()) {
            Piece piece = entry.getValue();
            int value = PieceValues.of(piece.type()) + PieceSquareTables.bonus(piece, entry.getKey(), endgame);
            score += piece.color() == color ? value : -value;
        }
        return endgame ? score + mopUp(pieces, color, score) : score;
    }

    private boolean isEndgame(Map<Square, Piece> pieces) {
        int material = 0;
        for (Piece piece : pieces.values()) {
            if (piece.type() != PieceType.Pawn) material += PieceValues.of(piece.type());
        }
        return material <= ENDGAME_MATERIAL;
    }

    private int mopUp(Map<Square, Piece> pieces, Color color, int score) {
        if (Math.abs(score) < 200) return 0;
        Color winner = score > 0 ? color : color.opposite();
        Square winnerKing = kingSquare(pieces, winner);
        Square loserKing = kingSquare(pieces, winner.opposite());
        int bonus = 10 * distanceFromCenter(loserKing) + 4 * (14 - distance(winnerKing, loserKing));
        return winner == color ? bonus : -bonus;
    }

    private Square kingSquare(Map<Square, Piece> pieces, Color color) {
        Piece king = Piece.of(color, PieceType.King);
        for (Map.Entry<Square, Piece> entry : pieces.entrySet()) {
            if (entry.getValue() == king) return entry.getKey();
        }
        throw new IllegalStateException("No king of color " + color);
    }

    private int distanceFromCenter(Square square) {
        int file = square.file().ordinal();
        int rank = square.rank().value() - 1;
        return Math.max(3 - file, file - 4) + Math.max(3 - rank, rank - 4);
    }

    private int distance(Square a, Square b) {
        return Math.abs(a.file().ordinal() - b.file().ordinal()) + Math.abs(a.rank().value() - b.rank().value());
    }
}
