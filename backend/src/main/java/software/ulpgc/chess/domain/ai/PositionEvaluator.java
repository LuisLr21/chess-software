package software.ulpgc.chess.domain.ai;

import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Piece;
import software.ulpgc.chess.domain.Position;

public class PositionEvaluator {
    public int evaluate(Position position, Color color) {
        int score = 0;
        for (Piece piece : position.board().pieces().values()) {
            int value = PieceValues.of(piece.type());
            score += piece.color() == color ? value : -value;
        }
        return score;
    }
}
