package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.ports.GameRepository;

public class GetGameUseCase implements UseCase<String, Game> {
    private final GameRepository repository;

    public GetGameUseCase(GameRepository repository) {
        this.repository = repository;
    }

    @Override
    public Game execute(String gameId) {
        return repository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }
}
