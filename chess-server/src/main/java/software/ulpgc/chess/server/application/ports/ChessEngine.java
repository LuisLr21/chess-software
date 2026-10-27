package software.ulpgc.chess.server.application.ports;

import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.core.Move;

import java.util.List;

public interface ChessEngine {
    List<String> levels();
    Move chooseMove(Game game, String level);
}
