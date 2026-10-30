package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.ports.ChessEngine;
import software.ulpgc.chess.server.application.ports.GameRepository;

public class ReviewMoveUseCase implements UseCase<MoveToReview, MoveReview> {
    private final GameRepository repository;
    private final ChessEngine engine;
    private final String level;

    public ReviewMoveUseCase(GameRepository repository, ChessEngine engine, String level) {
        this.repository = repository;
        this.engine = engine;
        this.level = level;
    }

    @Override
    public MoveReview execute(MoveToReview target) {
        Game game = repository.findById(target.gameId())
                .orElseThrow(() -> new GameNotFoundException(target.gameId()));
        int index = target.index();
        if (index < 0 || index >= game.history().size()) throw new IllegalArgumentException("No move at index " + index);

        Game before = Game.restore(game.id(), game.moves().subList(0, index), null, null);
        Game suggested = before.play(engine.chooseMove(before, level));
        return new MoveReview(game.history().get(index).san(), suggested.history().getLast().san());
    }
}
