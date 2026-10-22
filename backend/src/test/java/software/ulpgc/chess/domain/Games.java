package software.ulpgc.chess.domain;

public class Games {
    public static Game play(String... moves) {
        return play(Game.start("test"), moves);
    }

    public static Game play(Game game, String... moves) {
        for (String move : moves) {
            game = game.play(move(move));
        }
        return game;
    }

    public static Move move(String uci) {
        PieceType promotion = uci.length() == 5 ? PieceType.fromSymbol(uci.charAt(4)) : null;
        return new Move(Square.at(uci.substring(0, 2)), Square.at(uci.substring(2, 4)), promotion);
    }
}
