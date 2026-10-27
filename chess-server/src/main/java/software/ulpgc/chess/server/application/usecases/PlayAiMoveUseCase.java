package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.core.GameOverException;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.ports.ChessEngine;
import software.ulpgc.chess.server.application.ports.GameRepository;

public class PlayAiMoveUseCase implements UseCase<AiTurn, Game> {
    private final GameRepository repository;
    private final ChessEngine engine;

    public PlayAiMoveUseCase(GameRepository repository, ChessEngine engine) {
        this.repository = repository;
        this.engine = engine;
    }

    @Override
    public Game execute(AiTurn turn) {
        String level = turn.level();
        if (level == null || !engine.levels().contains(level)) throw new IllegalArgumentException("Unknown AI level: " + level);

        Game game = repository.findById(turn.gameId())
                .orElseThrow(() -> new GameNotFoundException(turn.gameId()));
        if (game.isOver()) throw new GameOverException(game.status());

        Game newGame = game.play(engine.chooseMove(game, level));
        repository.save(newGame);
        return newGame;
    }
}
