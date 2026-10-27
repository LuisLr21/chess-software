package software.ulpgc.chess.ai.application.usecases;

import software.ulpgc.chess.ai.application.UseCase;
import software.ulpgc.chess.ai.application.domain.Engine;
import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.core.GameOverException;
import software.ulpgc.chess.core.Move;

import java.util.Map;

public class ChooseMoveUseCase implements UseCase<BestMoveQuery, Move> {
    private final Map<String, Engine> engines;

    public ChooseMoveUseCase(Map<String, Engine> engines) {
        this.engines = engines;
    }

    @Override
    public Move execute(BestMoveQuery query) {
        Engine engine = engines.get(query.level());
        if (engine == null) throw new IllegalArgumentException("Unknown AI level: " + query.level());

        Game game = Game.restore("ai", query.moves(), null, null);
        if (game.isOver()) throw new GameOverException(game.status());

        return engine.chooseMove(game.position());
    }
}
