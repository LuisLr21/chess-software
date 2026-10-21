package software.ulpgc.chess.domain;

import java.util.ArrayList;
import java.util.List;

public record Game(String id, Position position) {
    private static final LegalMoveValidator validator = new LegalMoveValidator();

    public static Game start(String id) {
        return new Game(id, Position.initial());
    }

    public Game play(Square from, Square to) {
        if (!validator.isLegal(position, from, to)) throw new IllegalMoveException(from, to);
        return new Game(id, position.move(from, to));
    }

    public List<Square> legalMovesFrom(Square from) {
        List<Square> destinations = new ArrayList<>();
        for (File file : File.values()) {
            for (Rank rank : Rank.values()) {
                Square to = new Square(file, rank);
                if (validator.isLegal(position, from, to)) destinations.add(to);
            }
        }
        return destinations;
    }
}
