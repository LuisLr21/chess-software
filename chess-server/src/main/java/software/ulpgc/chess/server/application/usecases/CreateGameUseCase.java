package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.ports.GameRepository;

import java.util.UUID;

public class CreateGameUseCase implements UseCase<Void, Game> {
    private final GameRepository repository;

    public CreateGameUseCase(GameRepository repository) {
        this.repository = repository;
    }

    @Override
    public Game execute(Void nothing) {
        Game game = Game.start(UUID.randomUUID().toString());
        repository.save(game);
        return game;
    }
}
