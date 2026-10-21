package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.PieceType;
import software.ulpgc.chess.domain.Square;

public class MovePieceUseCase {
    private final GameRepository repository;

    public MovePieceUseCase(GameRepository repository) {
        this.repository = repository;
    }

    public Game execute(String gameId, Square from, Square to, PieceType promotion) {
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        Game newGame = game.play(from, to, promotion);
        repository.save(newGame);
        return newGame;
    }
}
