package software.ulpgc.chess.domain.ai;

import software.ulpgc.chess.domain.Move;
import software.ulpgc.chess.domain.Piece;
import software.ulpgc.chess.domain.Position;

import java.util.Comparator;
import java.util.List;

public class MoveOrdering {
    private static final int FIRST = Integer.MAX_VALUE;

    public List<Move> sort(Position position, List<Move> moves, Move first) {
        return moves.stream()
                .sorted(Comparator.comparingInt((Move move) -> priority(position, move, first)).reversed())
                .toList();
    }

    private int priority(Position position, Move move, Move first) {
        if (move.equals(first)) return FIRST;
        int priority = 0;
        Piece captured = position.capturedPiece(move.from(), move.to());
        if (captured != null) {
            Piece attacker = position.board().pieceAt(move.from());
            priority += 10 * PieceValues.of(captured.type()) - PieceValues.of(attacker.type());
        }
        if (move.promotion() != null) priority += PieceValues.of(move.promotion());
        return priority;
    }
}
