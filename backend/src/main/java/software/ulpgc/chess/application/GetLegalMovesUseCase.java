package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.Square;

import java.util.List;

public class GetLegalMovesUseCase {
    private final GameRepository repository;

    public GetLegalMovesUseCase(GameRepository repository) {
        this.repository = repository;
    }

    public List<Square> execute(String gameId, Square from) {
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        return game.legalMovesFrom(from);
    }
}
