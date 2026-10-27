package software.ulpgc.chess.ai.application.domain;

import software.ulpgc.chess.core.Move;
import software.ulpgc.chess.core.Position;

public interface Engine {
    Move chooseMove(Position position);
}
