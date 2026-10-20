package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Game;

public class GetGameUseCase {
    private final GameRepository repository;

    public GetGameUseCase(GameRepository repository) {
        this.repository = repository;
    }

    public Game execute(String gameId) {
        return repository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }
}
