package software.ulpgc.chess.domain;

public record Game(String id, Position position) {
    private static final LegalMoveValidator validator = new LegalMoveValidator();

    public static Game start(String id) {
        return new Game(id, Position.initial());
    }

    public Game play(Square from, Square to) {
        if (!validator.isLegal(position, from, to)) throw new IllegalMoveException(from, to);
        return new Game(id, position.move(from, to));
    }
}
