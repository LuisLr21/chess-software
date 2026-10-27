package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.ports.GameRepository;

public class MovePieceUseCase implements UseCase<GameMove, Game> {
    private final GameRepository repository;

    public MovePieceUseCase(GameRepository repository) {
        this.repository = repository;
    }

    @Override
    public Game execute(GameMove move) {
        Game game = repository.findById(move.gameId())
                .orElseThrow(() -> new GameNotFoundException(move.gameId()));

        Game newGame = game.play(move.from(), move.to(), move.promotion());
        repository.save(newGame);
        return newGame;
    }
}
