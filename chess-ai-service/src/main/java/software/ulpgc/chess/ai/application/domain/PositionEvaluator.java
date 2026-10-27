package software.ulpgc.chess.ai.application.domain;

import software.ulpgc.chess.core.Color;
import software.ulpgc.chess.core.Piece;
import software.ulpgc.chess.core.Position;

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
