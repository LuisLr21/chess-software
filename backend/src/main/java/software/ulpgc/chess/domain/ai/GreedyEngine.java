package software.ulpgc.chess.domain.ai;

import software.ulpgc.chess.domain.LegalMoveGenerator;
import software.ulpgc.chess.domain.Move;
import software.ulpgc.chess.domain.Piece;
import software.ulpgc.chess.domain.Position;

import java.util.List;
import java.util.Random;

public class GreedyEngine implements Engine{
    private final LegalMoveGenerator generator = new LegalMoveGenerator();
    private final Random random = new Random();

    @Override
    public Move chooseMove(Position position) {
        List<Move> moves = generator.allLegalMoves(position);
        Move best = moves.get(random.nextInt(moves.size()));
        int bestValue = 0;
        for (Move move : moves) {
            int value = captureValue(position, move);
            if (value > bestValue) {
                best = move;
                bestValue = value;
            }
        }
        return best;
    }

    private int captureValue(Position position, Move move) {
        Piece captured = position.capturedPiece(move.from(), move.to());
        return captured == null ? 0 : PieceValues.of(captured.type());
    }
}
