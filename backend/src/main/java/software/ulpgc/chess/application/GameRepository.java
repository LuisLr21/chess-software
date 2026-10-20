package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Game;

import java.util.Optional;

public interface GameRepository {
    void save(Game game);
    Optional<Game> findById(String id);
}
