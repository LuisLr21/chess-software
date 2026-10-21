package software.ulpgc.chess.domain;

import java.util.ArrayList;
import java.util.List;

public record Game(String id, Position position, List<Move> history) {
    private static final LegalMoveValidator validator = new LegalMoveValidator();
    private static final LegalMoveGenerator generator = new LegalMoveGenerator();
    private static final GameStatusEvaluator statusEvaluator = new GameStatusEvaluator();

    public Game {
        history = List.copyOf(history);
    }

    public static Game start(String id) {
        return new Game(id, Position.initial(), List.of());
    }

    public static Game replay(String id, List<Move> moves) {
        Game game = start(id);
        for (Move move : moves) {
            game = game.play(move.from(), move.to(), move.promotion() == null ? PieceType.Queen : move.promotion());
        }
        return game;
    }

    public Game play(Square from, Square to) {
        return play(from, to, PieceType.Queen);
    }

    public Game play(Square from, Square to, PieceType promotion) {
        if (isOver()) throw new GameOverException(status());
        if (promotion == PieceType.King || promotion == PieceType.Pawn) {
            throw new IllegalArgumentException("Invalid promotion piece: " + promotion);
        }
        if (!validator.isLegal(position, from, to)) throw new IllegalMoveException(from, to);
        Move move = new Move(from, to, position.isPromotion(from, to) ? promotion : null);
        return new Game(id, position.move(from, to, promotion), append(move));
    }

    public List<Square> legalMovesFrom(Square from) {
        if (isOver()) return List.of();
        return generator.legalMovesFrom(position, from);
    }

    public GameStatus status() {
        return statusEvaluator.evaluate(position);
    }

    public boolean isOver() {
        return status() != GameStatus.InProgress;
    }

    public Color winner() {
        return status() == GameStatus.Checkmate ? position.sideToMove().opposite() : null;
    }

    private List<Move> append(Move move) {
        List<Move> moves = new ArrayList<>(history);
        moves.add(move);
        return moves;
    }
}
