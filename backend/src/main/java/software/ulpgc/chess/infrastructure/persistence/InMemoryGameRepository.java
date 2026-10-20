package software.ulpgc.chess.infrastructure.persistence;

import software.ulpgc.chess.application.GameRepository;
import software.ulpgc.chess.domain.Game;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryGameRepository implements GameRepository {
    private final Map<String, Game> games = new ConcurrentHashMap<>();

    @Override
    public void save(Game game) {
        games.put(game.id(), game);
    }

    @Override
    public Optional<Game> findById(String id) {
        return Optional.ofNullable(games.get(id));
    }
}