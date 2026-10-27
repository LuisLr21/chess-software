package software.ulpgc.chess.server.application.ports;

import software.ulpgc.chess.core.Game;

import java.util.Optional;

public interface GameRepository {
    void save(Game game);
    Optional<Game> findById(String id);
}
