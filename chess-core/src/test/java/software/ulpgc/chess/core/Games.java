package software.ulpgc.chess.core;

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
        return Move.fromUci(uci);
    }
}
