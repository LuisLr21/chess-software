package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.core.Square;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.ports.GameRepository;

import java.util.List;

public class GetLegalMovesUseCase implements UseCase<SelectedPiece, List<Square>> {
    private final GameRepository repository;

    public GetLegalMovesUseCase(GameRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Square> execute(SelectedPiece selected) {
        Game game = repository.findById(selected.gameId())
                .orElseThrow(() -> new GameNotFoundException(selected.gameId()));

        return game.legalMovesFrom(selected.from());
    }
}
