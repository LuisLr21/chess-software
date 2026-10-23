package software.ulpgc.chess.domain.ai;

import software.ulpgc.chess.domain.*;

import java.time.Duration;
import java.util.List;

public class ExpertEngine implements Engine {
    private static final int MATE = 1_000_000;
    private static final int INFINITY = Integer.MAX_VALUE;

    private final LegalMoveGenerator generator = new LegalMoveGenerator();
    private final CheckDetector checkDetector = new CheckDetector();
    private final InsufficientMaterialDetector materialDetector = new InsufficientMaterialDetector();
    private final PositionalEvaluator evaluator = new PositionalEvaluator();
    private final MoveOrdering ordering = new MoveOrdering();
    private final Duration timeLimit;
    private final int maxDepth;

    public ExpertEngine(Duration timeLimit, int maxDepth) {
        this.timeLimit = timeLimit;
        this.maxDepth = maxDepth;
    }

    @Override
    public Move chooseMove(Position position) {
        long start = System.nanoTime();
        long deadline = start + timeLimit.toNanos();
        long softDeadline = start + timeLimit.toNanos() / 2;
        List<Move> moves = generator.allLegalMoves(position);
        if (moves.isEmpty()) return null;
        ScoredMove best = new ScoredMove(moves.getFirst(), 0);
        for (int depth = 1; depth <= maxDepth; depth++) {
            try {
                best = searchRoot(position, moves, depth, best.move(), depth == 1 ? Long.MAX_VALUE : deadline);
            } catch (SearchTimeout timeout) {
                break;
            }
            if (Math.abs(best.score()) >= MATE - maxDepth || System.nanoTime() > softDeadline) break;
        }
        return best.move();
    }

    private ScoredMove searchRoot(Position position, List<Move> moves, int depth, Move previousBest, long deadline) {
        ScoredMove best = null;
        int alpha = -INFINITY;
        for (Move move : ordering.sort(position, moves, previousBest)) {
            int score = -negamax(play(position, move), depth - 1, 1, -INFINITY, -alpha, deadline);
            if (best == null || score > best.score()) {
                best = new ScoredMove(move, score);
                alpha = Math.max(alpha, score);
            }
        }
        return best;
    }

    private int negamax(Position position, int depth, int ply, int alpha, int beta, long deadline) {
        if (System.nanoTime() > deadline) throw new SearchTimeout();
        if (isDraw(position)) return 0;
        boolean inCheck = checkDetector.isInCheck(position, position.sideToMove());
        if (depth == 0 && !inCheck) return quiescence(position, alpha, beta, deadline);
        List<Move> moves = generator.allLegalMoves(position);
        if (moves.isEmpty()) return inCheck ? -MATE + ply : 0;
        if (depth == 0) return quiescence(position, alpha, beta, deadline);
        for (Move move : ordering.sort(position, moves, null)) {
            int score = -negamax(play(position, move), depth - 1, ply + 1, -beta, -alpha, deadline);
            if (score >= beta) return beta;
            alpha = Math.max(alpha, score);
        }
        return alpha;
    }

    private int quiescence(Position position, int alpha, int beta, long deadline) {
        if (System.nanoTime() > deadline) throw new SearchTimeout();
        int standPat = evaluator.evaluate(position, position.sideToMove());
        if (standPat >= beta) return beta;
        alpha = Math.max(alpha, standPat);
        for (Move move : ordering.sort(position, generator.allLegalCaptures(position), null)) {
            int score = -quiescence(play(position, move), -beta, -alpha, deadline);
            if (score >= beta) return beta;
            alpha = Math.max(alpha, score);
        }
        return alpha;
    }

    private boolean isDraw(Position position) {
        return position.halfMoveClock() >= 100 || materialDetector.isInsufficient(position.board());
    }

    private Position play(Position position, Move move) {
        return position.move(move.from(), move.to(), move.promotion());
    }

    private record ScoredMove(Move move, int score) {}

    private static final class SearchTimeout extends RuntimeException {
        private SearchTimeout() {
            super(null, null, false, false);
        }
    }
}
