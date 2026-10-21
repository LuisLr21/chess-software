package software.ulpgc.chess.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record Game(String id, Position position, List<PlayedMove> history, Ending ending, ChessClock clock) {
    private static final LegalMoveValidator validator = new LegalMoveValidator();
    private static final LegalMoveGenerator generator = new LegalMoveGenerator();
    private static final GameStatusEvaluator statusEvaluator = new GameStatusEvaluator();
    private static final CheckDetector checkDetector = new CheckDetector();
    private static final SanFormatter sanFormatter = new SanFormatter();

    public Game {
        history = List.copyOf(history);
    }

    public static Game start(String id) {
        return start(id, null);
    }

    public static Game start(String id, ChessClock clock) {
        return new Game(id, Position.initial(), List.of(), null, clock);
    }

    public static Game restore(String id, List<Move> moves, Ending ending, ChessClock clock) {
        Game game = start(id);
        for (Move move : moves) {
            game = game.play(move.from(), move.to(), move.promotion() == null ? PieceType.Queen : move.promotion());
        }
        return new Game(id, game.position, game.history, ending, clock);
    }

    public Game play(Square from, Square to) {
        return play(from, to, PieceType.Queen);
    }

    public Game play(Square from, Square to, PieceType promotion) {
        return play(from, to, promotion, null);
    }

    public Game play(Square from, Square to, PieceType promotion, Instant now) {
        if (isOver()) throw new GameOverException(status());
        if (clock != null && now != null && clock.isExpired(position.sideToMove(), now)) throw new GameOverException(GameStatus.Timeout);
        if (promotion == PieceType.King || promotion == PieceType.Pawn) {
            throw new IllegalArgumentException("Invalid promotion piece: " + promotion);
        }
        if (!validator.isLegal(position, from, to)) throw new IllegalMoveException(from, to);
        Move move = new Move(from, to, position.isPromotion(from, to) ? promotion : null);
        Position after = position.move(from, to, promotion);
        PlayedMove played = new PlayedMove(move, sanFormatter.format(position, move, after), position.capturedPiece(from, to));
        Game next = new Game(id, after, append(played), null, clockAfterMove(now));
        return next.clock != null && now != null && next.isOver()
                ? new Game(id, after, next.history, null, next.clock.stop(after.sideToMove(), now))
                : next;
    }

    public Game startClock(Instant now) {
        if (clock == null || clock.isRunning()) return this;
        return new Game(id, position, history, ending, clock.start(now));
    }

    public Game resign(Color loser, Instant now) {
        return end(new Ending(GameStatus.Resignation, loser.opposite()), now);
    }

    public Game agreeDraw(Instant now) {
        return end(new Ending(GameStatus.DrawByAgreement, null), now);
    }

    public Game flagTimeout(Instant now) {
        if (isOver() || clock == null || !clock.isExpired(position.sideToMove(), now)) return this;
        return end(new Ending(GameStatus.Timeout, position.sideToMove().opposite()), now);
    }

    public List<Square> legalMovesFrom(Square from) {
        if (isOver()) return List.of();
        return generator.legalMovesFrom(position, from);
    }

    public GameStatus status() {
        return ending != null ? ending.status() : statusEvaluator.evaluate(position);
    }

    public boolean isOver() {
        return status() != GameStatus.InProgress;
    }

    public Color winner() {
        if (ending != null) return ending.winner();
        return status() == GameStatus.Checkmate ? position.sideToMove().opposite() : null;
    }

    public boolean isInCheck() {
        return checkDetector.isInCheck(position, position.sideToMove());
    }

    public List<Piece> capturedBy(Color color) {
        return history.stream()
                .map(PlayedMove::captured)
                .filter(piece -> piece != null && piece.color() == color.opposite())
                .toList();
    }

    public List<Move> moves() {
        return history.stream().map(PlayedMove::move).toList();
    }

    private Game end(Ending newEnding, Instant now) {
        if (isOver()) throw new GameOverException(status());
        ChessClock stopped = clock == null || now == null ? clock : clock.stop(position.sideToMove(), now);
        return new Game(id, position, history, newEnding, stopped);
    }

    private ChessClock clockAfterMove(Instant now) {
        if (clock == null || now == null) return clock;
        return clock.afterMove(position.sideToMove(), now);
    }

    private List<PlayedMove> append(PlayedMove move) {
        List<PlayedMove> moves = new ArrayList<>(history);
        moves.add(move);
        return moves;
    }
}
