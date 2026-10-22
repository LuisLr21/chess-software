package software.ulpgc.chess.domain.ai;

import software.ulpgc.chess.domain.Move;
import software.ulpgc.chess.domain.Position;

public interface Engine {
    Move chooseMove(Position position);
}
