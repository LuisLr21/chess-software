package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.core.Move;
import software.ulpgc.chess.server.application.ports.ChessEngine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FakeChessEngine implements ChessEngine {
    private final Map<String, Move> moves = new LinkedHashMap<>();

    public void plays(String level, String uci) {
        moves.put(level, uci == null ? null : Move.fromUci(uci));
    }

    @Override
    public List<String> levels() {
        return List.copyOf(moves.keySet());
    }

    @Override
    public Move chooseMove(Game game, String level) {
        return moves.get(level);
    }
}
