package software.ulpgc.chess.domain;

import java.util.List;

public record Game(String id, Position position) {
    private static final LegalMoveValidator validator = new LegalMoveValidator();
    private static final LegalMoveGenerator generator = new LegalMoveGenerator();
    private static final GameStatusEvaluator statusEvaluator = new GameStatusEvaluator();

    public static Game start(String id) {
        return new Game(id, Position.initial());
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
        return new Game(id, position.move(from, to, promotion));
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
}
