package software.ulpgc.chess.ai.application.domain;

import software.ulpgc.chess.core.Color;
import software.ulpgc.chess.core.GameStatus;
import software.ulpgc.chess.core.GameStatusEvaluator;
import software.ulpgc.chess.core.LegalMoveGenerator;
import software.ulpgc.chess.core.Move;
import software.ulpgc.chess.core.Position;

public class MinimaxEngine implements Engine{
    private static final int MATE = 1_000_000;

    private final LegalMoveGenerator generator = new LegalMoveGenerator();
    private final GameStatusEvaluator statusEvaluator = new GameStatusEvaluator();
    private final PositionEvaluator evaluator = new PositionEvaluator();
    private final int depth;

    public MinimaxEngine(int depth) {
        this.depth = depth;
    }

    @Override
    public Move chooseMove(Position position) {
        Color me = position.sideToMove();
        Move best = null;
        int bestValue = Integer.MIN_VALUE;
        for (Move move : generator.allLegalMoves(position)) {
            int value = minimax(play(position, move), depth -1, bestValue, Integer.MAX_VALUE, me);
            if (best == null || value > bestValue) {
                best = move;
                bestValue = value;
            }
        }
        return best;
    }

    private int minimax(Position position, int depth, int alpha, int beta, Color me) {
        GameStatus status = statusEvaluator.evaluate(position);
        if (status == GameStatus.Checkmate) return position.sideToMove() == me ? -MATE - depth : MATE + depth;
        if (status != GameStatus.InProgress) return 0;
        if (depth == 0) return evaluator.evaluate(position, me);

        boolean maximizing = position.sideToMove() == me;
        int best = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Move move : generator.allLegalMoves(position)) {
            int value = minimax(play(position, move), depth - 1, alpha, beta, me);
            if (maximizing) {
                best = Math.max(best, value);
                alpha = Math.max(alpha, best);
            } else {
                best = Math.min(best, value);
                beta = Math.min(beta, best);
            }
            if (alpha >= beta) break;
        }
        return best;
    }

    private Position play(Position position, Move move) {
        return position.move(move.from(), move.to(), move.promotion());
    }
}
