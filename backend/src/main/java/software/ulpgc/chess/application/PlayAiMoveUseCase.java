package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.GameOverException;
import software.ulpgc.chess.domain.ai.Engine;

import java.util.Map;

public class PlayAiMoveUseCase {
    private final GameRepository repository;
    private final Map<String, Engine> engines;

    public PlayAiMoveUseCase(GameRepository repository, Map<String, Engine> engines) {
        this.repository = repository;
        this.engines = engines;
    }

    public Game execute(String gameId, String level) {
        Engine engine = engines.get(level);
        if (engine == null) throw new IllegalArgumentException("Unknown AI level: " + level);

        Game game = repository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
        if (game.isOver()) throw new GameOverException(game.status());

        Game newGame = game.play(engine.chooseMove(game.position()));
        repository.save(newGame);
        return newGame;
    }
}
