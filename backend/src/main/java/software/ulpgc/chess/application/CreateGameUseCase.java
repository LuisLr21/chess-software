package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Game;

import java.util.UUID;

public class CreateGameUseCase {
    private final GameRepository repository;

    public CreateGameUseCase(GameRepository repository) {
        this.repository = repository;
    }

    public Game execute() {
        Game game = Game.start(UUID.randomUUID().toString());
        repository.save(game);
        return game;
    }
}
